package com.richardmogou.StageRichy.service; // Standard package

import java.util.Map;

public interface CalculationService {

    /**
     * Calculates the weighted average grade for a specific student across all subjects.
     * Takes into account subject coefficients.
     *
     * @param studentId The ID of the student.
     * @return The calculated weighted average, or Optional.empty() if the student has no grades.
     *         Returns Double.NaN if calculation is not possible (e.g., division by zero if total coefficient is 0).
     */
    Double calculateOverallAverage(Long studentId);

    /**
     * Calculates the weighted average grade for a specific student within a specific subject.
     *
     * @param studentId The ID of the student.
     * @param subjectId The ID of the subject.
     * @return The calculated average for the subject, or Optional.empty() if the student has no grades in that subject.
     */
    Double calculateSubjectAverage(Long studentId, Long subjectId);

    /**
     * Calculates the weighted average grade for each subject for a specific student.
     *
     * @param studentId The ID of the student.
     * @return A Map where the key is the Subject Name (String) and the value is the calculated average (Double) for that subject.
     *         Returns an empty map if the student has no grades.
     */
    Map<String, Double> calculateAllSubjectAverages(Long studentId);

    // Potential future additions:
    // - Calculate averages within a specific Semester
    // - Calculate class averages for a Subject
}