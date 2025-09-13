package com.richardmogou.dto; // Standard package

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeDto {
    private Long id;
    private Double score;
    private String comments;
    private LocalDate dateAssigned;

    // Include identifiers for related entities
    private Long studentId;
    private String studentUsername; // Useful for display
    private String studentFullName; // Combine first/last name

    private Long subjectId;
    private String subjectName; // Useful for display

    // Semester info
    private Long semesterId;
    private String semesterName;
}