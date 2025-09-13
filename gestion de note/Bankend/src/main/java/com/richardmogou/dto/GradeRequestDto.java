package com.richardmogou.dto; // Standard package

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeRequestDto {

    @NotNull(message = "Score cannot be null")
    @Min(value = 0, message = "Score must be at least 0")
    @Max(value = 100, message = "Score must be at most 100") // Adjust scale if needed
    private Double score;

    private String comments; // Optional comments

    @NotNull(message = "Student ID cannot be null")
    private Long studentId; // ID of the student receiving the grade

    @NotNull(message = "Subject ID cannot be null")
    private Long subjectId; // ID of the subject the grade is for

    // Semester ID (optional)
    private Long semesterId;
}