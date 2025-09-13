package com.richardmogou.controller;

import com.richardmogou.dto.StudentAcademicSummaryDto;
import com.richardmogou.model.User;
import com.richardmogou.service.StudentInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@CrossOrigin(origins = "*", maxAge = 3600) // Adjust for production
@RestController
@RequestMapping("/api/student/info") // Base path for student info operations
@PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')") // Secure endpoints for STUDENT or ADMIN
public class StudentInfoController {

    private static final Logger logger = LoggerFactory.getLogger(StudentInfoController.class);

    @Autowired
    private StudentInfoService studentInfoService;

    // Helper method to get current user's ID (could be moved to a utility class)
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            logger.error("Could not retrieve authenticated user details.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated properly.");
        }
        User userPrincipal = (User) authentication.getPrincipal();
        return userPrincipal.getId();
    }

    // GET /api/student/info/summary - Retrieve academic summary for the authenticated student
    @GetMapping("/summary")
    public ResponseEntity<StudentAcademicSummaryDto> getMyAcademicSummary() {
        Long studentId = getCurrentUserId();
        logger.info("Requesting academic summary for student ID: {}", studentId);

        return studentInfoService.getStudentAcademicSummary(studentId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> {
                    logger.warn("Academic summary could not be generated or student not found by service for ID: {}", studentId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Student academic summary not found.");
                });
    }
}