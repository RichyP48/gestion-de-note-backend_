package com.richardmogou.controller;

import com.richardmogou.dto.ClassSectionDto;
import com.richardmogou.dto.StudentDto;
import com.richardmogou.model.User;
import com.richardmogou.service.ClassSectionService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/teacher/classes")
@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'STUDENT')")
public class TeacherClassController {

    private static final Logger logger = LoggerFactory.getLogger(TeacherClassController.class);

    @Autowired
    private ClassSectionService classSectionService;

    // Helper method to get current user's ID
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            logger.error("Could not retrieve authenticated user details.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated properly.");
        }
        User userPrincipal = (User) authentication.getPrincipal();
        return userPrincipal.getId();
    }

    // GET /api/teacher/classes - Retrieve all classes assigned to the authenticated teacher
    @GetMapping
    public ResponseEntity<List<ClassSectionDto>> getMyClasses(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long semesterId) {
        Long teacherId = getCurrentUserId();
        logger.info("Fetching classes for teacher ID: {} (Subject filter: {}, Semester filter: {})", 
                teacherId, subjectId, semesterId);
        try {
            List<ClassSectionDto> classes = classSectionService.findAllClassSections(teacherId, subjectId, semesterId);
            return ResponseEntity.ok(classes);
        } catch (EntityNotFoundException e) {
            logger.warn("Error fetching classes for teacher {}: {}", teacherId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error fetching classes for teacher {}", teacherId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while fetching classes.");
        }
    }

    // GET /api/teacher/classes/{id} - Retrieve a specific class by ID
    @GetMapping("/{id}")
    public ResponseEntity<ClassSectionDto> getClassById(@PathVariable Long id) {
        Long teacherId = getCurrentUserId();
        logger.info("Fetching class ID: {} for teacher ID: {}", id, teacherId);

        return classSectionService.findClassSectionById(id)
                .map(classDto -> {
                    // Verify the class is assigned to the authenticated teacher
                    if (classDto.getTeacherId() != null && !classDto.getTeacherId().equals(teacherId)) {
                        logger.warn("Teacher {} attempted to access class {} assigned to teacher {}",
                                teacherId, id, classDto.getTeacherId());
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this class.");
                    }
                    return ResponseEntity.ok(classDto);
                })
                .orElseThrow(() -> {
                    logger.warn("Class not found with ID: {} for teacher {}", id, teacherId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found with ID: " + id);
                });
    }

    // GET /api/teacher/classes/{id}/students - Retrieve all students in a specific class
    @GetMapping("/{id}/students")
    public ResponseEntity<List<StudentDto>> getStudentsInClass(@PathVariable Long id) {
        Long teacherId = getCurrentUserId();
        logger.info("Fetching students for class ID: {} by teacher ID: {}", id, teacherId);

        // First check if the class exists and is assigned to the teacher
        ClassSectionDto classDto = classSectionService.findClassSectionById(id)
                .orElseThrow(() -> {
                    logger.warn("Class not found with ID: {} for teacher {}", id, teacherId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found with ID: " + id);
                });

        // Verify the class is assigned to the authenticated teacher
        if (classDto.getTeacherId() != null && !classDto.getTeacherId().equals(teacherId)) {
            logger.warn("Teacher {} attempted to access students in class {} assigned to teacher {}",
                    teacherId, id, classDto.getTeacherId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to students in this class.");
        }

        try {
            List<StudentDto> students = classSectionService.findStudentsByClassSectionId(id);
            return ResponseEntity.ok(students);
        } catch (EntityNotFoundException e) {
            logger.warn("Error fetching students for class {}: {}", id, e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error fetching students for class {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while fetching students.");
        }
    }
}
