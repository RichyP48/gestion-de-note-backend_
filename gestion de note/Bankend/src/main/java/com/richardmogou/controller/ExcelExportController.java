package com.richardmogou.controller;

import com.richardmogou.dto.MessageResponse;
import com.richardmogou.service.ExcelService;
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

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/excel")
public class ExcelExportController {

    private static final Logger logger = LoggerFactory.getLogger(ExcelExportController.class);

    @Autowired
    private ExcelService excelService;


    @GetMapping("/student/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or (hasRole('STUDENT') and @userSecurity.isCurrentUser(#id))")
    public ResponseEntity<?> exportStudentGrades(@PathVariable Long id) {
        logger.info("Exporting Excel report for student ID: {}", id);

        try {
            ByteArrayInputStream excelStream = excelService.generateStudentGradesExcel(id);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=student_grades_" + id + ".xlsx");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(excelStream));

        } catch (EntityNotFoundException e) {
            logger.warn("Cannot export Excel: Student not found with ID: {}", id);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Error: " + e.getMessage()));
        } catch (IOException e) {
            logger.error("Error generating Excel report for student ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error generating Excel report: " + e.getMessage()));
        }
    }

    // GET /api/excel/class/{id} - Export grades for a specific class
    @GetMapping("/class/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TEACHER') and @userSecurity.isTeacherOfClass(#id))")
    public ResponseEntity<?> exportClassGrades(@PathVariable Long id) {
        logger.info("Exporting Excel report for class ID: {}", id);

        try {
            ByteArrayInputStream excelStream = excelService.generateClassGradesExcel(id);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=class_grades_" + id + ".xlsx");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(excelStream));

        } catch (EntityNotFoundException | IllegalStateException e) {
            logger.warn("Cannot export Excel: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Error: " + e.getMessage()));
        } catch (IOException e) {
            logger.error("Error generating Excel report for class ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error generating Excel report: " + e.getMessage()));
        }
    }

    // GET /api/excel/subject/{id} - Export grades for a specific subject
    @GetMapping("/subject/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<?> exportSubjectGrades(
            @PathVariable Long id,
            @RequestParam(required = false) Long semesterId) {
        
        logger.info("Exporting Excel report for subject ID: {} and semester ID: {}", id, semesterId);

        try {
            ByteArrayInputStream excelStream = excelService.generateSubjectGradesExcel(id, semesterId);

            String filename = "subject_grades_" + id;
            if (semesterId != null) {
                filename += "_semester_" + semesterId;
            }
            filename += ".xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(excelStream));

        } catch (EntityNotFoundException e) {
            logger.warn("Cannot export Excel: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Error: " + e.getMessage()));
        } catch (IOException e) {
            logger.error("Error generating Excel report for subject ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error generating Excel report: " + e.getMessage()));
        }
    }

    // GET /api/excel/semester/{id} - Export grades for a specific semester
    @GetMapping("/semester/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<?> exportSemesterGrades(@PathVariable Long id) {
        logger.info("Exporting Excel report for semester ID: {}", id);

        try {
            ByteArrayInputStream excelStream = excelService.generateSemesterGradesExcel(id);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=semester_grades_" + id + ".xlsx");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(excelStream));

        } catch (EntityNotFoundException e) {
            logger.warn("Cannot export Excel: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Error: " + e.getMessage()));
        } catch (IOException e) {
            logger.error("Error generating Excel report for semester ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error generating Excel report: " + e.getMessage()));
        }
    }

    // GET /api/excel/all - Export all grades (admin only)
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> exportAllGrades() {
        logger.info("Exporting Excel report for all grades");

        try {
            ByteArrayInputStream excelStream = excelService.generateAllGradesExcel();

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=all_grades.xlsx");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(excelStream));

        } catch (IOException e) {
            logger.error("Error generating Excel report for all grades: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error generating Excel report: " + e.getMessage()));
        }
    }
}
