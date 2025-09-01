package com.richardmogou.StageRichy.service;

import com.richardmogou.StageRichy.dto.ClassSectionDto;
import com.richardmogou.StageRichy.dto.ClassSectionRequestDto;
import com.richardmogou.StageRichy.dto.StudentDto;
import com.richardmogou.StageRichy.model.*;
import com.richardmogou.StageRichy.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClassSectionServiceImpl implements ClassSectionService {

    @Autowired
    private ClassSectionRepository classSectionRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private StudentRepository studentRepository;

    // --- Helper Methods for Mapping ---
    private ClassSectionDto mapToDto(ClassSection classSection) {
        if (classSection == null) {
            return null;
        }
        
        Subject subject = classSection.getSubject();
        Semester semester = classSection.getSemester();
        Teacher teacher = classSection.getTeacher();
        
        // Map students to DTOs
        List<StudentDto> studentDtos = classSection.getStudents().stream()
                .map(this::mapStudentToDto)
                .collect(Collectors.toList());
        
        return new ClassSectionDto(
                classSection.getId(),
                classSection.getName(),
                (subject != null) ? subject.getId() : null,
                (subject != null) ? subject.getName() : null,
                (semester != null) ? semester.getId() : null,
                (semester != null) ? semester.getName() : null,
                (teacher != null) ? teacher.getId() : null,
                (teacher != null) ? teacher.getUsername() : null,
                (teacher != null) ? teacher.getFirstName() + " " + teacher.getLastName() : null,
                studentDtos,
                studentDtos.size()
        );
    }
    
    private StudentDto mapStudentToDto(Student student) {
        if (student == null) {
            return null;
        }
        
        return new StudentDto(
                student.getId(),
                student.getUsername(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail()
        );
    }

    // --- Service Method Implementations ---

    @Override
    @Transactional(readOnly = true)
    public List<ClassSectionDto> findAllClassSections(Long teacherId, Long subjectId, Long semesterId) {
        List<ClassSection> classSections;
        
        // Fetch entities if IDs are provided
        Teacher teacher = null;
        Subject subject = null;
        Semester semester = null;
        
        if (teacherId != null) {
            teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new EntityNotFoundException("Teacher not found with ID: " + teacherId));
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
        if (teacher != null && subject != null && semester != null) {
            classSections = classSectionRepository.findByTeacherAndSubjectAndSemester(teacher, subject, semester);
        } else if (teacher != null && subject != null) {
            classSections = classSectionRepository.findByTeacherAndSubject(teacher, subject);
        } else if (teacher != null && semester != null) {
            classSections = classSectionRepository.findByTeacherAndSemester(teacher, semester);
        } else if (subject != null && semester != null) {
            classSections = classSectionRepository.findBySubjectAndSemester(subject, semester);
        } else if (teacher != null) {
            classSections = classSectionRepository.findByTeacher(teacher);
        } else if (subject != null) {
            classSections = classSectionRepository.findBySubject(subject);
        } else if (semester != null) {
            classSections = classSectionRepository.findBySemester(semester);
        } else {
            classSections = classSectionRepository.findAll();
        }
        
        return classSections.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClassSectionDto> findClassSectionById(Long id) {
        return classSectionRepository.findById(id).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentDto> findStudentsByClassSectionId(Long classSectionId) {
        ClassSection classSection = classSectionRepository.findById(classSectionId)
                .orElseThrow(() -> new EntityNotFoundException("Class section not found with ID: " + classSectionId));
        
        return classSection.getStudents().stream()
                .map(this::mapStudentToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassSectionDto> findClassSectionsByStudentId(Long studentId, Long semesterId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with ID: " + studentId));
        
        List<ClassSection> classSections;
        
        if (semesterId != null) {
            Semester semester = semesterRepository.findById(semesterId)
                    .orElseThrow(() -> new EntityNotFoundException("Semester not found with ID: " + semesterId));
            
            classSections = classSectionRepository.findByStudentsContainingAndSemester(student, semester);
        } else {
            classSections = classSectionRepository.findByStudentsContaining(student);
        }
        
        return classSections.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ClassSectionDto createClassSection(ClassSectionRequestDto classSectionRequestDto) {
        // Fetch related entities
        Subject subject = subjectRepository.findById(classSectionRequestDto.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with ID: " + classSectionRequestDto.getSubjectId()));
        
        Semester semester = semesterRepository.findById(classSectionRequestDto.getSemesterId())
                .orElseThrow(() -> new IllegalArgumentException("Semester not found with ID: " + classSectionRequestDto.getSemesterId()));
        
        // Create new ClassSection entity
        ClassSection newClassSection = new ClassSection();
        newClassSection.setName(classSectionRequestDto.getName());
        newClassSection.setSubject(subject);
        newClassSection.setSemester(semester);
        
        // Set teacher if provided
        if (classSectionRequestDto.getTeacherId() != null) {
            Teacher teacher = teacherRepository.findById(classSectionRequestDto.getTeacherId())
                    .orElseThrow(() -> new IllegalArgumentException("Teacher not found with ID: " + classSectionRequestDto.getTeacherId()));
            
            newClassSection.setTeacher(teacher);
        }
        
        // Add students if provided
        if (classSectionRequestDto.getStudentIds() != null && !classSectionRequestDto.getStudentIds().isEmpty()) {
            for (Long studentId : classSectionRequestDto.getStudentIds()) {
                Student student = studentRepository.findById(studentId)
                        .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));
                
                newClassSection.addStudent(student);
            }
        }
        
        ClassSection savedClassSection = classSectionRepository.save(newClassSection);
        return mapToDto(savedClassSection);
    }

    @Override
    @Transactional
    public Optional<ClassSectionDto> updateClassSection(Long id, ClassSectionRequestDto classSectionRequestDto) {
        Optional<ClassSection> existingClassSectionOptional = classSectionRepository.findById(id);
        if (existingClassSectionOptional.isEmpty()) {
            return Optional.empty(); // ClassSection not found
        }
        
        ClassSection existingClassSection = existingClassSectionOptional.get();
        
        // Update fields
        existingClassSection.setName(classSectionRequestDto.getName());
        
        // Update subject if changed
        if (!existingClassSection.getSubject().getId().equals(classSectionRequestDto.getSubjectId())) {
            Subject subject = subjectRepository.findById(classSectionRequestDto.getSubjectId())
                    .orElseThrow(() -> new IllegalArgumentException("Subject not found with ID: " + classSectionRequestDto.getSubjectId()));
            
            existingClassSection.setSubject(subject);
        }
        
        // Update semester if changed
        if (!existingClassSection.getSemester().getId().equals(classSectionRequestDto.getSemesterId())) {
            Semester semester = semesterRepository.findById(classSectionRequestDto.getSemesterId())
                    .orElseThrow(() -> new IllegalArgumentException("Semester not found with ID: " + classSectionRequestDto.getSemesterId()));
            
            existingClassSection.setSemester(semester);
        }
        
        // Update teacher if provided
        if (classSectionRequestDto.getTeacherId() != null) {
            Teacher teacher = teacherRepository.findById(classSectionRequestDto.getTeacherId())
                    .orElseThrow(() -> new IllegalArgumentException("Teacher not found with ID: " + classSectionRequestDto.getTeacherId()));
            
            existingClassSection.setTeacher(teacher);
        }
        
        ClassSection updatedClassSection = classSectionRepository.save(existingClassSection);
        return Optional.of(mapToDto(updatedClassSection));
    }

    @Override
    @Transactional
    public boolean deleteClassSection(Long id) {
        if (classSectionRepository.existsById(id)) {
            classSectionRepository.deleteById(id);
            return true;
        }
        return false; // ClassSection not found
    }

    @Override
    @Transactional
    public ClassSectionDto enrollStudent(Long classSectionId, Long studentId) {
        ClassSection classSection = classSectionRepository.findById(classSectionId)
                .orElseThrow(() -> new EntityNotFoundException("Class section not found with ID: " + classSectionId));
        
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with ID: " + studentId));
        
        classSection.addStudent(student);
        ClassSection updatedClassSection = classSectionRepository.save(classSection);
        
        return mapToDto(updatedClassSection);
    }

    @Override
    @Transactional
    public ClassSectionDto removeStudent(Long classSectionId, Long studentId) {
        ClassSection classSection = classSectionRepository.findById(classSectionId)
                .orElseThrow(() -> new EntityNotFoundException("Class section not found with ID: " + classSectionId));
        
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with ID: " + studentId));
        
        classSection.removeStudent(student);
        ClassSection updatedClassSection = classSectionRepository.save(classSection);
        
        return mapToDto(updatedClassSection);
    }

    @Override
    @Transactional
    public ClassSectionDto assignTeacher(Long classSectionId, Long teacherId) {
        ClassSection classSection = classSectionRepository.findById(classSectionId)
                .orElseThrow(() -> new EntityNotFoundException("Class section not found with ID: " + classSectionId));
        
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found with ID: " + teacherId));
        
        classSection.setTeacher(teacher);
        ClassSection updatedClassSection = classSectionRepository.save(classSection);
        
        return mapToDto(updatedClassSection);
    }
}
