package com.richardmogou.service;

import com.richardmogou.dto.StatisticsDto;

/**
 * Service for calculating various statistics related to grades.
 */
public interface StatisticsService {
    
    /**
     * Calculate statistics for a specific student.
     * 
     * @param studentId The ID of the student
     * @param semesterId Optional semester ID to filter grades
     * @return StatisticsDto containing the calculated statistics
     */
    StatisticsDto calculateStudentStatistics(Long studentId, Long semesterId);
    
    /**
     * Calculate statistics for a specific subject.
     * 
     * @param subjectId The ID of the subject
     * @param semesterId Optional semester ID to filter grades
     * @return StatisticsDto containing the calculated statistics
     */
    StatisticsDto calculateSubjectStatistics(Long subjectId, Long semesterId);
    
    /**
     * Calculate statistics for a specific class section.
     * 
     * @param classSectionId The ID of the class section
     * @return StatisticsDto containing the calculated statistics
     */
    StatisticsDto calculateClassStatistics(Long classSectionId);
    
    /**
     * Calculate statistics for a specific semester.
     * 
     * @param semesterId The ID of the semester
     * @return StatisticsDto containing the calculated statistics
     */
    StatisticsDto calculateSemesterStatistics(Long semesterId);
    
    /**
     * Calculate overall system statistics.
     * 
     * @return StatisticsDto containing the calculated statistics
     */
    StatisticsDto calculateOverallStatistics();
}
