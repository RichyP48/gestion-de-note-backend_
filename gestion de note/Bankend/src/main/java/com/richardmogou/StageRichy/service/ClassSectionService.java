package com.richardmogou.StageRichy.service;

import com.richardmogou.StageRichy.dto.ClassSectionDto;
import com.richardmogou.StageRichy.dto.ClassSectionRequestDto;
import com.richardmogou.StageRichy.dto.StudentDto;

import java.util.List;
import java.util.Optional;

public interface ClassSectionService {

    /**
     * Retrieves all class sections, potentially filtered by teacher, subject, or semester ID.
     * @param teacherId Optional ID of the teacher to filter by.
     * @param subjectId Optional ID of the subject to filter by.
     * @param semesterId Optional ID of the semester to filter by.
     * @return A list of ClassSection DTOs matching the criteria.
     */
    List<ClassSectionDto> findAllClassSections(Long teacherId, Long subjectId, Long semesterId);

    /**
     * Finds a specific class section by its ID.
     * @param id The ID of the class section.
     * @return An Optional containing the ClassSection DTO if found, otherwise empty.
     */
    Optional<ClassSectionDto> findClassSectionById(Long id);

    /**
     * Finds all students enrolled in a specific class section.
     * @param classSectionId The ID of the class section.
     * @return A list of Student DTOs enrolled in the class section.
     */
    List<StudentDto> findStudentsByClassSectionId(Long classSectionId);

    /**
     * Finds all class sections that a specific student is enrolled in.
     * @param studentId The ID of the student.
     * @param semesterId Optional ID of the semester to filter by.
     * @return A list of ClassSection DTOs that the student is enrolled in.
     */
    List<ClassSectionDto> findClassSectionsByStudentId(Long studentId, Long semesterId);

    /**
     * Creates a new class section.
     * @param classSectionRequestDto DTO containing the details for the new class section.
     * @return The created ClassSection DTO.
     */
    ClassSectionDto createClassSection(ClassSectionRequestDto classSectionRequestDto);

    /**
     * Updates an existing class section.
     * @param id The ID of the class section to update.
     * @param classSectionRequestDto DTO containing the updated details.
     * @return An Optional containing the updated ClassSection DTO if found and updated, otherwise empty.
     */
    Optional<ClassSectionDto> updateClassSection(Long id, ClassSectionRequestDto classSectionRequestDto);

    /**
     * Deletes a class section by its ID.
     * @param id The ID of the class section to delete.
     * @return true if the class section was found and deleted, false otherwise.
     */
    boolean deleteClassSection(Long id);

    /**
     * Enrolls a student in a class section.
     * @param classSectionId The ID of the class section.
     * @param studentId The ID of the student.
     * @return The updated ClassSection DTO.
     */
    ClassSectionDto enrollStudent(Long classSectionId, Long studentId);

    /**
     * Removes a student from a class section.
     * @param classSectionId The ID of the class section.
     * @param studentId The ID of the student.
     * @return The updated ClassSection DTO.
     */
    ClassSectionDto removeStudent(Long classSectionId, Long studentId);

    /**
     * Assigns a teacher to a class section.
     * @param classSectionId The ID of the class section.
     * @param teacherId The ID of the teacher.
     * @return The updated ClassSection DTO.
     */
    ClassSectionDto assignTeacher(Long classSectionId, Long teacherId);
}
