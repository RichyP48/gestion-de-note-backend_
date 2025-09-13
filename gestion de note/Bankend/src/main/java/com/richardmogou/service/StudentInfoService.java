package com.richardmogou.service;

import com.richardmogou.dto.StudentAcademicSummaryDto;

import java.util.Optional;

public interface StudentInfoService {

    /**
     * Retrieves a comprehensive academic summary for a given student.
     * Includes student details, enrolled classes (with subject, teacher, semester),
     * grades grouped by subject, and overall average.
     *
     * @param studentId The ID of the student.
     * @return An Optional containing the StudentAcademicSummaryDto if the student is found, otherwise empty.
     */
    Optional<StudentAcademicSummaryDto> getStudentAcademicSummary(Long studentId);

}