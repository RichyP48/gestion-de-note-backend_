package com.richardmogou.controller;

import com.richardmogou.dto.MessageResponse;
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
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private static final Logger logger = LoggerFactory.getLogger(AdminReportController.class);

    @Autowired
    private PdfService pdfService;

    // GET /api/admin/reports/student/{id} - Generate a PDF report for a specific student
    @GetMapping("/student/{id}")
    public ResponseEntity<InputStreamResource> generateStudentReport(@PathVariable Long id) {
        logger.info("Generating PDF report for student ID: {}", id);

        try {
            ByteArrayInputStream pdfInputStream = pdfService.generateStudentGradeReport(id);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=grade_report_" + id + ".pdf");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(pdfInputStream));

        } catch (EntityNotFoundException e) {
            logger.warn("Cannot generate report: Student not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            logger.error("Error generating PDF report for student ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            logger.error("Unexpected error during PDF report generation for student ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // POST /api/admin/reports/bulk - Generate PDF reports for multiple students
    @PostMapping("/bulk")
    public ResponseEntity<?> generateBulkReports(@RequestBody List<Long> studentIds) {
        logger.info("Generating bulk PDF reports for {} students", studentIds.size());

        if (studentIds.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: No student IDs provided"));
        }

        try {
            ByteArrayInputStream zipInputStream = pdfService.generateBulkStudentReports(studentIds);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=student_reports.zip");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(zipInputStream));

        } catch (EntityNotFoundException e) {
            logger.warn("Cannot generate bulk reports: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Error: " + e.getMessage()));
        } catch (IOException e) {
            logger.error("Error generating bulk PDF reports: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error generating bulk reports: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during bulk PDF report generation: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Unexpected error generating bulk reports: " + e.getMessage()));
        }
    }

    // POST /api/admin/reports/class/{id} - Generate PDF reports for all students in a class
    @PostMapping("/class/{id}")
    public ResponseEntity<?> generateClassReports(@PathVariable Long id) {
        logger.info("Generating PDF reports for all students in class ID: {}", id);

        try {
            ByteArrayInputStream zipInputStream = pdfService.generateClassReports(id);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=class_" + id + "_reports.zip");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(zipInputStream));

        } catch (EntityNotFoundException e) {
            logger.warn("Cannot generate class reports: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Error: " + e.getMessage()));
        } catch (IOException e) {
            logger.error("Error generating PDF reports for class ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error generating class reports: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during class PDF report generation for class ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Unexpected error generating class reports: " + e.getMessage()));
        }
    }

    // POST /api/admin/reports/semester/{id} - Generate PDF reports for all students in a semester
    @PostMapping("/semester/{id}")
    public ResponseEntity<?> generateSemesterReports(@PathVariable Long id) {
        logger.info("Generating PDF reports for all students in semester ID: {}", id);

        try {
            ByteArrayInputStream zipInputStream = pdfService.generateSemesterReports(id);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=semester_" + id + "_reports.zip");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(zipInputStream));

        } catch (EntityNotFoundException e) {
            logger.warn("Cannot generate semester reports: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Error: " + e.getMessage()));
        } catch (IOException e) {
            logger.error("Error generating PDF reports for semester ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error generating semester reports: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during semester PDF report generation for semester ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Unexpected error generating semester reports: " + e.getMessage()));
        }
    }
}
