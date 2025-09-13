package com.richardmogou.service; // Standard package

import com.richardmogou.dto.SubjectDto;
import com.richardmogou.dto.SubjectRequestDto;

import java.util.List;
import java.util.Optional;

public interface SubjectService {

    /**
     * Retrieves all subjects.
     * @return A list of all Subject DTOs.
     */
    List<SubjectDto> findAllSubjects();

    /**
     * Finds a subject by its ID.
     * @param id The ID of the subject.
     * @return An Optional containing the Subject DTO if found, otherwise empty.
     */
    Optional<SubjectDto> findSubjectById(Long id);

    /**
     * Creates a new subject.
     * @param subjectRequestDto DTO containing the details for the new subject.
     * @return The created Subject DTO.
     * @throws IllegalArgumentException if a subject with the same name already exists.
     */
    SubjectDto createSubject(SubjectRequestDto subjectRequestDto);

    /**
     * Updates an existing subject.
     * @param id The ID of the subject to update.
     * @param subjectRequestDto DTO containing the updated details.
     * @return An Optional containing the updated Subject DTO if found and updated, otherwise empty.
     * @throws IllegalArgumentException if trying to update to a name that already exists (and belongs to another subject).
     */
    Optional<SubjectDto> updateSubject(Long id, SubjectRequestDto subjectRequestDto);

    /**
     * Deletes a subject by its ID.
     * @param id The ID of the subject to delete.
     * @return true if the subject was found and deleted, false otherwise.
     */
    boolean deleteSubject(Long id);
}