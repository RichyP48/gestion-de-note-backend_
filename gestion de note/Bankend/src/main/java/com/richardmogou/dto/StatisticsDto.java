package com.richardmogou.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Data Transfer Object for grade statistics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsDto {
    
    // Basic statistics
    private Double averageScore;
    private Double medianScore;
    private Double minScore;
    private Double maxScore;
    private Double standardDeviation;
    
    // Count information
    private Integer totalGrades;
    private Integer totalStudents;
    private Integer totalSubjects;
    
    // Distribution information
    private Integer passingGrades; // Grades >= 60%
    private Integer failingGrades; // Grades < 60%
    private Double passingRate; // Percentage of passing grades
    
    // Grade distribution (A, B, C, D, F)
    private Map<String, Integer> gradeDistribution;
    
    // Subject performance (if applicable)
    private Map<String, Double> subjectAverages;
    
    // Student performance (if applicable, limited to top performers)
    private Map<String, Double> topStudentAverages;
    
    // Context information
    private String statisticsType; // "student", "subject", "class", "semester", "overall"
    private Long contextId; // ID of the student, subject, class, or semester (null for overall)
    private String contextName; // Name of the student, subject, class, or semester (null for overall)
    private Long semesterId; // ID of the semester if filtered (null if not applicable)
    private String semesterName; // Name of the semester if filtered (null if not applicable)
}
