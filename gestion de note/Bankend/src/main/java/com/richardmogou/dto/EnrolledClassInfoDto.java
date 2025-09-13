package com.richardmogou.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrolledClassInfoDto {
    private Long classSectionId;
    private String classSectionName; // e.g., "MATH-101-A"
    private Long subjectId;
    private String subjectName;
    private Long teacherId; // Can be null if no teacher assigned
    private String teacherFullName; // e.g., "Jane Doe" (can be null)
    private Long semesterId;
    private String semesterName;
}