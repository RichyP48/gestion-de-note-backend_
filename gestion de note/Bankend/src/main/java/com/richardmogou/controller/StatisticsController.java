package com.richardmogou.controller;

import com.richardmogou.dto.MessageResponse;
import com.richardmogou.dto.StatisticsDto;
import com.richardmogou.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/statistics")
@Tag(name = "Statistics", description = "Grade statistics and analytics operations")
@SecurityRequirement(name = "bearerAuth")
public class StatisticsController implements SwaggerConfig.StatisticsTag {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);

    @Autowired
    private StatisticsService statisticsService;

    @Operation(summary = "Get statistics for a specific student", description = "Returns statistical analysis of grades for a specific student, optionally filtered by semester")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StatisticsDto.class))),
        @ApiResponse(responseCode = "404", description = "Student not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class)))
    })
    @GetMapping("/student/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or (hasRole('STUDENT') and @userSecurity.isCurrentUser(#id))")
    public ResponseEntity<?> getStudentStatistics(
            @Parameter(description = "ID of the student to get statistics for", required = true)
            @PathVariable Long id,
            @Parameter(description = "Optional semester ID to filter statistics by semester")
            @RequestParam(required = false) Long semesterId) {
        
        logger.info("Fetching statistics for student ID: {} and semester ID: {}", id, semesterId);
        
        try {
            StatisticsDto statistics = statisticsService.calculateStudentStatistics(id, semesterId);
            return ResponseEntity.ok(statistics);
        } catch (EntityNotFoundException e) {
            logger.warn("Cannot fetch statistics: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error calculating student statistics: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error calculating statistics: " + e.getMessage()));
        }
    }

    // GET /api/statistics/subject/{id} - Get statistics for a specific subject
    @GetMapping("/subject/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<?> getSubjectStatistics(
            @PathVariable Long id,
            @RequestParam(required = false) Long semesterId) {
        
        logger.info("Fetching statistics for subject ID: {} and semester ID: {}", id, semesterId);
        
        try {
            StatisticsDto statistics = statisticsService.calculateSubjectStatistics(id, semesterId);
            return ResponseEntity.ok(statistics);
        } catch (EntityNotFoundException e) {
            logger.warn("Cannot fetch statistics: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error calculating subject statistics: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error calculating statistics: " + e.getMessage()));
        }
    }

    // GET /api/statistics/class/{id} - Get statistics for a specific class
    @GetMapping("/class/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TEACHER') and @userSecurity.isTeacherOfClass(#id))")
    public ResponseEntity<?> getClassStatistics(@PathVariable Long id) {
        logger.info("Fetching statistics for class ID: {}", id);
        
        try {
            StatisticsDto statistics = statisticsService.calculateClassStatistics(id);
            return ResponseEntity.ok(statistics);
        } catch (EntityNotFoundException e) {
            logger.warn("Cannot fetch statistics: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error calculating class statistics: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error calculating statistics: " + e.getMessage()));
        }
    }

    // GET /api/statistics/semester/{id} - Get statistics for a specific semester
    @GetMapping("/semester/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<?> getSemesterStatistics(@PathVariable Long id) {
        logger.info("Fetching statistics for semester ID: {}", id);
        
        try {
            StatisticsDto statistics = statisticsService.calculateSemesterStatistics(id);
            return ResponseEntity.ok(statistics);
        } catch (EntityNotFoundException e) {
            logger.warn("Cannot fetch statistics: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error calculating semester statistics: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error calculating statistics: " + e.getMessage()));
        }
    }

    // GET /api/statistics/overall - Get overall system statistics
    @GetMapping("/overall")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getOverallStatistics() {
        logger.info("Fetching overall statistics");
        
        try {
            StatisticsDto statistics = statisticsService.calculateOverallStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Error calculating overall statistics: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error calculating statistics: " + e.getMessage()));
        }
    }
}
