package com.richardmogou.controller; // Standard package

import com.richardmogou.dto.GradeDto;
import com.richardmogou.dto.GradeRequestDto;
import com.richardmogou.dto.MessageResponse;
import com.richardmogou.service.GradeService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600) // Adjust for production
@RestController
@RequestMapping("/api/teacher/grades") // Base path for teacher grade operations
@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')") // Secure endpoints for TEACHER or ADMIN
public class TeacherGradeController {

    private static final Logger logger = LoggerFactory.getLogger(TeacherGradeController.class);

    @Autowired
    private GradeService gradeService;

    // GET /api/teacher/grades - Retrieve grades (potentially filtered)
    // Teachers might only see grades they are related to (e.g., their subjects/students)
    // Admins might see all. This filtering logic could be added in the service layer.
    // For now, providing basic filtering by student/subject ID.
    @GetMapping
    public ResponseEntity<List<GradeDto>> getGrades(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long semesterId) {
        // TODO: Implement authorization logic in service:
        // Ensure teacher can only access grades for their assigned students/subjects.
        // Admin can access all.
        try {
            List<GradeDto> grades = gradeService.findAllGrades(studentId, subjectId, semesterId);
            return ResponseEntity.ok(grades);
        } catch (EntityNotFoundException e) {
             logger.warn("Error fetching grades: {}", e.getMessage());
             throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // GET /api/teacher/grades/{id} - Retrieve a specific grade by ID
    @GetMapping("/{id}")
    public ResponseEntity<GradeDto> getGradeById(@PathVariable Long id) {
        // TODO: Implement authorization logic in service: Ensure teacher can access this specific grade.
        return gradeService.findGradeById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> {
                    logger.warn("Grade not found with ID: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Grade not found with ID: " + id);
                });
    }

    // POST /api/teacher/grades - Create a new grade entry
    @PostMapping
    public ResponseEntity<?> createGrade(@Valid @RequestBody GradeRequestDto gradeRequestDto) {
        // TODO: Implement authorization logic in service: Ensure teacher can assign grade for this student/subject.
        try {
            GradeDto createdGrade = gradeService.createGrade(gradeRequestDto);
            logger.info("Created grade with ID: {}", createdGrade.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdGrade);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to create grade, invalid reference: {}", e.getMessage());
            // Return 400 Bad Request if student/subject ID is invalid
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating grade for student {} / subject {}", gradeRequestDto.getStudentId(), gradeRequestDto.getSubjectId(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error creating grade: " + e.getMessage()));
        }
    }

    // PUT /api/teacher/grades/{id} - Update an existing grade
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGrade(@PathVariable Long id, @Valid @RequestBody GradeRequestDto gradeRequestDto) {
         // TODO: Implement authorization logic in service: Ensure teacher can update this specific grade.
        try {
            return gradeService.updateGrade(id, gradeRequestDto)
                    .map(updatedGrade -> {
                        logger.info("Updated grade with ID: {}", id);
                        return ResponseEntity.ok(updatedGrade);
                    })
                    .orElseThrow(() -> {
                        logger.warn("Grade not found for update with ID: {}", id);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Grade not found with ID: " + id);
                    });
        } catch (IllegalArgumentException e) {
             // This might occur if update logic tries to change student/subject and fails validation
             logger.warn("Failed to update grade {}, invalid reference: {}", id, e.getMessage());
             return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
             logger.error("Error updating grade with ID: {}", id, e);
             return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error updating grade: " + e.getMessage()));
        }
    }

    // DELETE /api/teacher/grades/{id} - Delete a grade
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteGrade(@PathVariable Long id) {
        // TODO: Implement authorization logic in service: Ensure teacher can delete this specific grade.
        boolean deleted = gradeService.deleteGrade(id);
        if (deleted) {
            logger.info("Deleted grade with ID: {}", id);
            return ResponseEntity.ok(new MessageResponse("Grade deleted successfully!"));
        } else {
            logger.warn("Grade not found for deletion with ID: {}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Grade not found with ID: " + id);
        }
    }
}