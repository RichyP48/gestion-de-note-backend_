package com.richardmogou.service; // Standard package

import com.richardmogou.dto.GradeDto;
import com.richardmogou.dto.GradeRequestDto;

import java.util.List;
import java.util.Optional;

public interface GradeService {

    /**
     * Retrieves all grades, potentially filtered by student, subject, or semester ID.
     * @param studentId Optional ID of the student to filter by.
     * @param subjectId Optional ID of the subject to filter by.
     * @param semesterId Optional ID of the semester to filter by.
     * @return A list of Grade DTOs matching the criteria.
     */
    List<GradeDto> findAllGrades(Long studentId, Long subjectId, Long semesterId);

    /**
     * Finds a specific grade by its ID.
     * @param id The ID of the grade.
     * @return An Optional containing the Grade DTO if found, otherwise empty.
     */
    Optional<GradeDto> findGradeById(Long id);

    /**
     * Creates a new grade entry.
     * @param gradeRequestDto DTO containing the details for the new grade.
     * @return The created Grade DTO.
     * @throws IllegalArgumentException if the specified student or subject does not exist.
     */
    GradeDto createGrade(GradeRequestDto gradeRequestDto);

    /**
     * Updates an existing grade.
     * @param id The ID of the grade to update.
     * @param gradeRequestDto DTO containing the updated details (score, comments).
     *                      Note: Changing studentId or subjectId via update might be disallowed or handled carefully.
     * @return An Optional containing the updated Grade DTO if found and updated, otherwise empty.
     * @throws IllegalArgumentException if the specified student or subject in the DTO does not exist (if changing is allowed).
     */
    Optional<GradeDto> updateGrade(Long id, GradeRequestDto gradeRequestDto);

    /**
     * Deletes a grade by its ID.
     * @param id The ID of the grade to delete.
     * @return true if the grade was found and deleted, false otherwise.
     */
    boolean deleteGrade(Long id);

}