package com.richardmogou.service; // Standard package

import com.richardmogou.dto.SubjectDto;
import com.richardmogou.dto.SubjectRequestDto;
import com.richardmogou.model.Subject;
import com.richardmogou.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SubjectServiceImpl implements SubjectService {

    @Autowired
    private SubjectRepository subjectRepository;

    // --- Helper Method for Mapping ---
    private SubjectDto mapToDto(Subject subject) {
        return new SubjectDto(subject.getId(), subject.getName(), subject.getCoefficient());
    }

    // --- Service Method Implementations ---

    @Override
    @Transactional(readOnly = true)
    public List<SubjectDto> findAllSubjects() {
        return subjectRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SubjectDto> findSubjectById(Long id) {
        return subjectRepository.findById(id)
                .map(this::mapToDto);
    }

    @Override
    @Transactional // Read-write transaction
    public SubjectDto createSubject(SubjectRequestDto subjectRequestDto) {
        // Check if subject with the same name already exists (case-insensitive)
        if (subjectRepository.existsByNameIgnoreCase(subjectRequestDto.getName())) {
            throw new IllegalArgumentException("Subject with name '" + subjectRequestDto.getName() + "' already exists.");
        }

        Subject newSubject = new Subject();
        newSubject.setName(subjectRequestDto.getName());
        newSubject.setCoefficient(subjectRequestDto.getCoefficient());

        Subject savedSubject = subjectRepository.save(newSubject);
        return mapToDto(savedSubject);
    }

    @Override
    @Transactional // Read-write transaction
    public Optional<SubjectDto> updateSubject(Long id, SubjectRequestDto subjectRequestDto) {
        Optional<Subject> existingSubjectOptional = subjectRepository.findById(id);
        if (existingSubjectOptional.isEmpty()) {
            return Optional.empty(); // Subject not found
        }

        Subject existingSubject = existingSubjectOptional.get();

        // Check if the new name conflicts with another existing subject
        Optional<Subject> subjectWithNewName = subjectRepository.findByNameIgnoreCase(subjectRequestDto.getName());
        if (subjectWithNewName.isPresent() && !subjectWithNewName.get().getId().equals(id)) {
            // A different subject already has the target name
            throw new IllegalArgumentException("Another subject with name '" + subjectRequestDto.getName() + "' already exists.");
        }

        // Update fields
        existingSubject.setName(subjectRequestDto.getName());
        existingSubject.setCoefficient(subjectRequestDto.getCoefficient());

        Subject updatedSubject = subjectRepository.save(existingSubject);
        return Optional.of(mapToDto(updatedSubject));
    }

    @Override
    @Transactional // Read-write transaction
    public boolean deleteSubject(Long id) {
        if (subjectRepository.existsById(id)) {
            // Consider adding checks here: e.g., cannot delete if grades are associated?
            // For now, simple deletion.
            subjectRepository.deleteById(id);
            return true;
        }
        return false; // Subject not found
    }
}