package com.richardmogou.StageRichy.controller; // Standard package

import com.richardmogou.StageRichy.dto.MessageResponse;
import com.richardmogou.StageRichy.dto.SubjectDto;
import com.richardmogou.StageRichy.dto.SubjectRequestDto;
import com.richardmogou.StageRichy.service.SubjectService;
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

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin/subjects") // Base path for subject admin operations
@PreAuthorize("hasRole('ADMIN')") // Secure all endpoints in this controller for ADMIN role
public class AdminSubjectController {

    private static final Logger logger = LoggerFactory.getLogger(AdminSubjectController.class);

    @Autowired
    private SubjectService subjectService;

    // GET /api/admin/subjects - Retrieve all subjects
    @GetMapping
    public ResponseEntity<List<SubjectDto>> getAllSubjects() {
        List<SubjectDto> subjects = subjectService.findAllSubjects();
        return ResponseEntity.ok(subjects);
    }

    // GET /api/admin/subjects/{id} - Retrieve a single subject by ID
    @GetMapping("/{id}")
    public ResponseEntity<SubjectDto> getSubjectById(@PathVariable Long id) {
        return subjectService.findSubjectById(id)
                .map(ResponseEntity::ok) // If found, wrap in ResponseEntity.ok()
                .orElseThrow(() -> {
                    logger.warn("Subject not found with ID: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found with ID: " + id);
                });
    }

    // POST /api/admin/subjects - Create a new subject
    @PostMapping
    public ResponseEntity<?> createSubject(@Valid @RequestBody SubjectRequestDto subjectRequestDto) {
        try {
            SubjectDto createdSubject = subjectService.createSubject(subjectRequestDto);
            logger.info("Created subject: {}", createdSubject.getName());
            // Return 201 Created status with the created subject DTO
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSubject);
        } catch (IllegalArgumentException e) {
            // Handle case where subject name already exists
            logger.warn("Failed to create subject, name conflict: {}", subjectRequestDto.getName());
            return ResponseEntity
                    .status(HttpStatus.CONFLICT) // 409 Conflict is appropriate here
                    .body(new MessageResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating subject: {}", subjectRequestDto.getName(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error creating subject: " + e.getMessage()));
        }
    }

    // PUT /api/admin/subjects/{id} - Update an existing subject
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSubject(@PathVariable Long id, @Valid @RequestBody SubjectRequestDto subjectRequestDto) {
        try {
            return subjectService.updateSubject(id, subjectRequestDto)
                    .map(updatedSubject -> {
                        logger.info("Updated subject with ID: {}", id);
                        return ResponseEntity.ok(updatedSubject); // Return 200 OK with updated DTO
                    })
                    .orElseThrow(() -> {
                        logger.warn("Subject not found for update with ID: {}", id);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found with ID: " + id);
                    });
        } catch (IllegalArgumentException e) {
            // Handle case where updated name conflicts with another subject
             logger.warn("Failed to update subject {}, name conflict: {}", id, subjectRequestDto.getName());
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new MessageResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
             logger.error("Error updating subject with ID: {}", id, e);
             return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error updating subject: " + e.getMessage()));
        }
    }

    // DELETE /api/admin/subjects/{id} - Delete a subject
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteSubject(@PathVariable Long id) {
        boolean deleted = subjectService.deleteSubject(id);
        if (deleted) {
            logger.info("Deleted subject with ID: {}", id);
            return ResponseEntity.ok(new MessageResponse("Subject deleted successfully!")); // 200 OK
        } else {
            logger.warn("Subject not found for deletion with ID: {}", id);
            // Return 404 Not Found if the subject didn't exist
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found with ID: " + id);
        }
        }
}