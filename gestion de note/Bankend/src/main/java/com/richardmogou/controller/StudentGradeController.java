package com.richardmogou.controller; // Standard package

import com.richardmogou.dto.GradeDto;
import com.richardmogou.model.User;
import com.richardmogou.service.GradeService;
import com.richardmogou.service.PdfService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600) // Adjust for production
@RestController
@RequestMapping("/api/student/grades") // Base path for student grade viewing operations
@PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')") // Secure endpoints for STUDENT or ADMIN
public class StudentGradeController {

    private static final Logger logger = LoggerFactory.getLogger(StudentGradeController.class);

    @Autowired
    private GradeService gradeService;

    @Autowired
    private PdfService pdfService; // Inject PdfService

    // Helper method to get current user's ID
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            // This should ideally not happen if security is configured correctly
            logger.error("Could not retrieve authenticated user details.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated properly.");
        }
        User userPrincipal = (User) authentication.getPrincipal();
        return userPrincipal.getId();
    }

    // GET /api/student/grades/my - Retrieve all grades for the authenticated student
    @GetMapping("/my")
    public ResponseEntity<List<GradeDto>> getMyGrades(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long semesterId) {
        Long studentId = getCurrentUserId();
        logger.info("Fetching grades for student ID: {} (Subject filter: {}, Semester filter: {})", 
                studentId, subjectId, semesterId);
        try {
            // Use the existing service method, filtering by the authenticated student's ID
            List<GradeDto> grades = gradeService.findAllGrades(studentId, subjectId, semesterId);
            return ResponseEntity.ok(grades);
        } catch (EntityNotFoundException e) {
            // This might happen if the subjectId or semesterId filter is invalid, but the student exists
             logger.warn("Error fetching grades for student {}: {}", studentId, e.getMessage());
             throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error fetching grades for student {}", studentId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while fetching grades.");
        }
    }

    // GET /api/student/grades/{id} - Retrieve a specific grade by ID (for the authenticated student)
    @GetMapping("/{id}")
    public ResponseEntity<GradeDto> getMyGradeById(@PathVariable Long id) {
        Long studentId = getCurrentUserId();
        logger.info("Fetching grade ID: {} for student ID: {}", id, studentId);

        return gradeService.findGradeById(id)
                .map(gradeDto -> {
                    // Verify the grade belongs to the authenticated student
                    if (!gradeDto.getStudentId().equals(studentId)) {
                        logger.warn("Student {} attempted to access grade {} belonging to student {}",
                                    studentId, id, gradeDto.getStudentId());
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this grade.");
                    }
                    return ResponseEntity.ok(gradeDto);
                })
                .orElseThrow(() -> {
                    logger.warn("Grade not found with ID: {} for student {}", id, studentId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Grade not found with ID: " + id);
                });
    }

    // Note: Filtering by semester would require adding semester logic to GradeService.findAllGrades
    // and potentially a SemesterRepository lookup here or in the service.

    // GET /api/student/grades/my/download - Download grade report PDF for the authenticated student
    @GetMapping("/my/download")
    public ResponseEntity<InputStreamResource> downloadMyGradeReport() {
        Long studentId = getCurrentUserId();
        logger.info("Requesting grade report PDF download for student ID: {}", studentId);

        try {
            ByteArrayInputStream pdfInputStream = pdfService.generateStudentGradeReport(studentId);

            HttpHeaders headers = new HttpHeaders();
            // Suggest filename for download
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=grade_report_" + studentId + ".pdf");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(pdfInputStream));

        } catch (EntityNotFoundException e) {
             logger.warn("Cannot generate report: Student not found with ID: {}", studentId);
             // Return 404 directly as ResponseEntity
             return ResponseEntity.notFound().build();
        } catch (IOException e) {
            logger.error("Error generating PDF report for student ID {}: {}", studentId, e.getMessage(), e);
            // Return 500 Internal Server Error
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
             logger.error("Unexpected error during PDF report generation for student ID {}: {}", studentId, e.getMessage(), e);
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}