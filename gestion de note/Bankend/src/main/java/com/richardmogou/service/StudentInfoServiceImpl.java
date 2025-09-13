package com.richardmogou.service;

import com.richardmogou.dto.EnrolledClassInfoDto;
import com.richardmogou.dto.GradeDto;
import com.richardmogou.dto.StudentAcademicSummaryDto;

import com.richardmogou.model.*;
import com.richardmogou.repository.GradeRepository;
import com.richardmogou.repository.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudentInfoServiceImpl implements StudentInfoService {

    private static final Logger logger = LoggerFactory.getLogger(StudentInfoServiceImpl.class);

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private GradeRepository gradeRepository; // Needed to fetch grades

    @Autowired
    private CalculationService calculationService; // Needed for overall average

    // Inject GradeService to reuse its DTO mapping logic
    @Autowired
    private GradeService gradeService;

    @Override
    @Transactional(readOnly = true)
    public Optional<StudentAcademicSummaryDto> getStudentAcademicSummary(Long studentId) {
        Optional<Student> studentOptional = studentRepository.findById(studentId);
        if (studentOptional.isEmpty()) {
            logger.warn("Student not found with ID: {}", studentId);
            return Optional.empty();
        }

        Student student = studentOptional.get();
        logger.debug("Found student: {}", student.getUsername());

        StudentAcademicSummaryDto summary = new StudentAcademicSummaryDto();
        summary.setStudentId(student.getId());
        summary.setUsername(student.getUsername());
        summary.setFirstName(student.getFirstName());
        summary.setLastName(student.getLastName());
        summary.setEmail(student.getEmail());

        // Fetch enrolled classes and map to DTOs
        List<EnrolledClassInfoDto> enrolledClassInfos = student.getEnrolledClasses().stream()
                .map(this::mapClassSectionToDto)
                .collect(Collectors.toList());
        summary.setEnrolledClasses(enrolledClassInfos);
        logger.debug("Mapped {} enrolled classes for student {}", enrolledClassInfos.size(), studentId);


        // Fetch all grades for the student using GradeService to get GradeDtos
        List<GradeDto> allGrades = gradeService.findAllGrades(studentId, null, null); // Pass null for semesterId to get all

        // Group grades by subject name
        Map<String, List<GradeDto>> gradesBySubject = allGrades.stream()
                .filter(g -> g.getSubjectName() != null) // Ensure subject name exists
                .collect(Collectors.groupingBy(GradeDto::getSubjectName));
        summary.setGradesBySubject(gradesBySubject);
        logger.debug("Grouped grades into {} subjects for student {}", gradesBySubject.size(), studentId);


        // Calculate overall average
        try {
            Double overallAvg = calculationService.calculateOverallAverage(studentId);
            summary.setOverallAverage(overallAvg);
             logger.debug("Calculated overall average {} for student {}", overallAvg, studentId);
        } catch (EntityNotFoundException e) {
            // Should not happen if student was found earlier, but log just in case
            logger.error("EntityNotFoundException during average calculation for student {}: {}", studentId, e.getMessage());
            summary.setOverallAverage(null); // Or Double.NaN
        } catch (Exception e) {
             logger.error("Error calculating overall average for student {}: {}", studentId, e.getMessage(), e);
             summary.setOverallAverage(null); // Or Double.NaN
        }


        return Optional.of(summary);
    }

    // Helper to map ClassSection to EnrolledClassInfoDto
    private EnrolledClassInfoDto mapClassSectionToDto(ClassSection section) {
        if (section == null) return null;

        Subject subject = section.getSubject();
        Teacher teacher = section.getTeacher();
        Semester semester = section.getSemester();

        String teacherFullName = null;
        if (teacher != null) {
            teacherFullName = teacher.getFirstName() + " " + teacher.getLastName();
        }

        return new EnrolledClassInfoDto(
                section.getId(),
                section.getName(),
                subject != null ? subject.getId() : null,
                subject != null ? subject.getName() : null,
                teacher != null ? teacher.getId() : null,
                teacherFullName,
                semester != null ? semester.getId() : null,
                semester != null ? semester.getName() : null
        );
    }
}