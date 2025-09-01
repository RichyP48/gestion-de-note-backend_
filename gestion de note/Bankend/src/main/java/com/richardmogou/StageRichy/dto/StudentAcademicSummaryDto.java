package com.richardmogou.StageRichy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentAcademicSummaryDto {
    private Long studentId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;

    private List<EnrolledClassInfoDto> enrolledClasses;

    // Grades grouped by Subject Name for easy display
    private Map<String, List<GradeDto>> gradesBySubject;

    // Optional: Overall calculated average
    private Double overallAverage;
}