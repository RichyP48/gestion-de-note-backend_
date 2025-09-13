package com.richardmogou.service; // Standard package

import com.richardmogou.dto.GradeDto;
import com.richardmogou.dto.GradeRequestDto;
import com.richardmogou.model.Grade;
import com.richardmogou.model.Semester;
import com.richardmogou.model.Student;
import com.richardmogou.model.Subject;
import com.richardmogou.repository.GradeRepository;
import com.richardmogou.repository.SemesterRepository;
import com.richardmogou.repository.StudentRepository;
import com.richardmogou.repository.SubjectRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GradeServiceImpl implements GradeService {

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    // --- Helper Method for Mapping ---
    private GradeDto mapToDto(Grade grade) {
        if (grade == null) {
            return null;
        }
        Student student = grade.getStudent();
        Subject subject = grade.getSubject();
        Semester semester = grade.getSemester();

        String studentFullName = (student != null) ? student.getFirstName() + " " + student.getLastName() : null;

        return new GradeDto(
                grade.getId(),
                grade.getScore(),
                grade.getComments(),
                grade.getDateAssigned(),
                (student != null) ? student.getId() : null,
                (student != null) ? student.getUsername() : null,
                studentFullName,
                (subject != null) ? subject.getId() : null,
                (subject != null) ? subject.getName() : null,
                (semester != null) ? semester.getId() : null,
                (semester != null) ? semester.getName() : null
        );
    }

    // --- Service Method Implementations ---

    @Override
    @Transactional(readOnly = true)
    public List<GradeDto> findAllGrades(Long studentId, Long subjectId, Long semesterId) {
        List<Grade> grades;
        
        // Fetch entities if IDs are provided
        Student student = null;
        Subject subject = null;
        Semester semester = null;
        
        if (studentId != null) {
            student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new EntityNotFoundException("Student not found with ID: " + studentId));
        }
        
        if (subjectId != null) {
            subject = subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new EntityNotFoundException("Subject not found with ID: " + subjectId));
        }
        
        if (semesterId != null) {
            semester = semesterRepository.findById(semesterId)
                    .orElseThrow(() -> new EntityNotFoundException("Semester not found with ID: " + semesterId));
        }
        
        // Apply filters based on which parameters are provided
        if (student != null && subject != null && semester != null) {
            // Filter by student, subject, and semester
            grades = gradeRepository.findByStudentAndSubjectAndSemester(student, subject, semester);
        } else if (student != null && subject != null) {
            // Filter by student and subject
            grades = gradeRepository.findByStudentAndSubject(student, subject);
        } else if (student != null && semester != null) {
            // Filter by student and semester
            grades = gradeRepository.findByStudentAndSemester(student, semester);
        } else if (subject != null && semester != null) {
            // Filter by subject and semester
            grades = gradeRepository.findBySubjectAndSemester(subject, semester);
        } else if (student != null) {
            // Filter by student only
            grades = gradeRepository.findByStudent(student);
        } else if (subject != null) {
            // Filter by subject only
            grades = gradeRepository.findBySubject(subject);
        } else if (semester != null) {
            // Filter by semester only
            grades = gradeRepository.findBySemester(semester);
        } else {
            // No filters, return all grades
            grades = gradeRepository.findAll();
        }
        
        return grades.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GradeDto> findGradeById(Long id) {
        return gradeRepository.findById(id).map(this::mapToDto);
    }

    @Override
    @Transactional
    public GradeDto createGrade(GradeRequestDto gradeRequestDto) {
        // Fetch related entities
        Student student = studentRepository.findById(gradeRequestDto.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + gradeRequestDto.getStudentId()));
        Subject subject = subjectRepository.findById(gradeRequestDto.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with ID: " + gradeRequestDto.getSubjectId()));

        // Create new Grade entity
        Grade newGrade = new Grade();
        newGrade.setScore(gradeRequestDto.getScore());
        newGrade.setComments(gradeRequestDto.getComments());
        newGrade.setStudent(student);
        newGrade.setSubject(subject);
        
        // Set semester if provided
        if (gradeRequestDto.getSemesterId() != null) {
            Semester semester = semesterRepository.findById(gradeRequestDto.getSemesterId())
                    .orElseThrow(() -> new IllegalArgumentException("Semester not found with ID: " + gradeRequestDto.getSemesterId()));
            newGrade.setSemester(semester);
        }
        // newGrade.setDateAssigned(...) // Handled by default in Grade entity

        Grade savedGrade = gradeRepository.save(newGrade);
        return mapToDto(savedGrade);
    }

    @Override
    @Transactional
    public Optional<GradeDto> updateGrade(Long id, GradeRequestDto gradeRequestDto) {
        Optional<Grade> existingGradeOptional = gradeRepository.findById(id);
        if (existingGradeOptional.isEmpty()) {
            return Optional.empty(); // Grade not found
        }

        Grade existingGrade = existingGradeOptional.get();

        // Typically, only score and comments are updated.
        // Updating student/subject might require different logic or be disallowed.
        // If student/subject update is needed, fetch and validate them like in createGrade:
        /*
        if (!existingGrade.getStudent().getId().equals(gradeRequestDto.getStudentId())) {
             Student student = studentRepository.findById(gradeRequestDto.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + gradeRequestDto.getStudentId()));
             existingGrade.setStudent(student);
        }
         if (!existingGrade.getSubject().getId().equals(gradeRequestDto.getSubjectId())) {
             Subject subject = subjectRepository.findById(gradeRequestDto.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with ID: " + gradeRequestDto.getSubjectId()));
             existingGrade.setSubject(subject);
        }
        */

        existingGrade.setScore(gradeRequestDto.getScore());
        existingGrade.setComments(gradeRequestDto.getComments());
        // existingGrade.setDateAssigned(...) // Usually not updated, maybe lastModifiedDate?

        Grade updatedGrade = gradeRepository.save(existingGrade);
        return Optional.of(mapToDto(updatedGrade));
    }

    @Override
    @Transactional
    public boolean deleteGrade(Long id) {
        if (gradeRepository.existsById(id)) {
            gradeRepository.deleteById(id);
            return true;
        }
        return false; // Grade not found
    }
}