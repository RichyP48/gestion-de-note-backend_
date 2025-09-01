package com.richardmogou.StageRichy.service; // Standard package

import com.richardmogou.StageRichy.model.Grade;
import com.richardmogou.StageRichy.model.Student;
import com.richardmogou.StageRichy.model.Subject;
import com.richardmogou.StageRichy.repository.GradeRepository;
import com.richardmogou.StageRichy.repository.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CalculationServiceImpl implements CalculationService {

    private static final Logger logger = LoggerFactory.getLogger(CalculationServiceImpl.class);

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Override
    @Transactional(readOnly = true)
    public Double calculateOverallAverage(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with ID: " + studentId));

        List<Grade> grades = gradeRepository.findByStudent(student);
        if (grades.isEmpty()) {
            logger.warn("No grades found for student ID: {}", studentId);
            return null; // Or return 0.0, depending on desired behavior for no grades
        }

        // Group grades by subject
        Map<Subject, List<Grade>> gradesBySubject = grades.stream()
                .filter(g -> g.getSubject() != null && g.getScore() != null) // Ensure subject and score are not null
                .collect(Collectors.groupingBy(Grade::getSubject));

        double totalWeightedScoreSum = 0;
        double totalCoefficientSum = 0;

        for (Map.Entry<Subject, List<Grade>> entry : gradesBySubject.entrySet()) {
            Subject subject = entry.getKey();
            List<Grade> subjectGrades = entry.getValue();

            if (subject.getCoefficient() == null || subject.getCoefficient() <= 0) {
                 logger.warn("Subject '{}' (ID: {}) has invalid coefficient {}, skipping for overall average.",
                           subject.getName(), subject.getId(), subject.getCoefficient());
                 continue; // Skip subjects with zero or null coefficient
            }

            // Calculate simple average for this subject's grades
            double subjectScoreSum = subjectGrades.stream().mapToDouble(Grade::getScore).sum();
            double subjectAverage = subjectScoreSum / subjectGrades.size();

            // Add to overall weighted sum
            totalWeightedScoreSum += subjectAverage * subject.getCoefficient();
            totalCoefficientSum += subject.getCoefficient();
        }

        if (totalCoefficientSum == 0) {
            logger.warn("Total coefficient sum is 0 for student ID: {}, cannot calculate weighted average.", studentId);
            // Return NaN or throw exception, depending on requirements
            return Double.NaN;
        }

        return totalWeightedScoreSum / totalCoefficientSum;
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateSubjectAverage(Long studentId, Long subjectId) {
         Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with ID: " + studentId));

         // Fetch grades specifically for this student and subject
         // This requires SubjectRepository or fetching all grades and filtering
         // Let's fetch all and filter for simplicity here, optimize later if needed
         List<Grade> subjectGrades = gradeRepository.findByStudent(student).stream()
                 .filter(g -> g.getSubject() != null && g.getSubject().getId().equals(subjectId) && g.getScore() != null)
                 .collect(Collectors.toList());

        if (subjectGrades.isEmpty()) {
            logger.debug("No grades found for student ID: {} in subject ID: {}", studentId, subjectId);
            return null; // No grades for this subject
        }

        double subjectScoreSum = subjectGrades.stream().mapToDouble(Grade::getScore).sum();
        return subjectScoreSum / subjectGrades.size();
    }

     @Override
    @Transactional(readOnly = true)
    public Map<String, Double> calculateAllSubjectAverages(Long studentId) {
         Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with ID: " + studentId));

        List<Grade> grades = gradeRepository.findByStudent(student);
        if (grades.isEmpty()) {
            return Collections.emptyMap();
        }

        // Group grades by subject
        Map<Subject, List<Grade>> gradesBySubject = grades.stream()
                .filter(g -> g.getSubject() != null && g.getScore() != null)
                .collect(Collectors.groupingBy(Grade::getSubject));

        // Calculate average for each subject
        return gradesBySubject.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getName(), // Key: Subject Name
                        entry -> {
                            List<Grade> subjectGrades = entry.getValue();
                            double sum = subjectGrades.stream().mapToDouble(Grade::getScore).sum();
                            return sum / subjectGrades.size(); // Value: Subject Average
                        }
                ));
    }
}