package com.richardmogou.service;

import com.richardmogou.dto.StatisticsDto;

import com.richardmogou.model.*;
import com.richardmogou.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsServiceImpl.class);
    
    // Grade letter boundaries
    private static final double A_THRESHOLD = 90.0;
    private static final double B_THRESHOLD = 80.0;
    private static final double C_THRESHOLD = 70.0;
    private static final double D_THRESHOLD = 60.0;
    private static final double PASSING_THRESHOLD = 60.0;
    
    // Maximum number of top students to include in statistics
    private static final int MAX_TOP_STUDENTS = 5;

    @Autowired
    private GradeRepository gradeRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private SubjectRepository subjectRepository;
    
    @Autowired
    private SemesterRepository semesterRepository;
    
    @Autowired
    private ClassSectionRepository classSectionRepository;

    /**
     * Helper method to calculate basic statistics from a list of grades
     */
    private StatisticsDto calculateBasicStatistics(List<Grade> grades, String statisticsType,
                                                   Long contextId, String contextName,
                                                   Long semesterId, String semesterName) {
        StatisticsDto statistics = new StatisticsDto();
        
        // Set context information
        statistics.setStatisticsType(statisticsType);
        statistics.setContextId(contextId);
        statistics.setContextName(contextName);
        statistics.setSemesterId(semesterId);
        statistics.setSemesterName(semesterName);
        
        // If no grades, return empty statistics
        if (grades.isEmpty()) {
            statistics.setTotalGrades(0);
            statistics.setAverageScore(0.0);
            statistics.setMedianScore(0.0);
            statistics.setMinScore(0.0);
            statistics.setMaxScore(0.0);
            statistics.setStandardDeviation(0.0);
            statistics.setPassingGrades(0);
            statistics.setFailingGrades(0);
            statistics.setPassingRate(0.0);
            statistics.setGradeDistribution(new HashMap<>());
            return statistics;
        }
        
        // Extract scores and filter out nulls
        List<Double> scores = grades.stream()
                .map(Grade::getScore)
                .filter(score -> score != null)
                .collect(Collectors.toList());
        
        // Calculate basic statistics
        int totalGrades = scores.size();
        statistics.setTotalGrades(totalGrades);
        
        if (totalGrades == 0) {
            // No valid scores
            statistics.setAverageScore(0.0);
            statistics.setMedianScore(0.0);
            statistics.setMinScore(0.0);
            statistics.setMaxScore(0.0);
            statistics.setStandardDeviation(0.0);
            statistics.setPassingGrades(0);
            statistics.setFailingGrades(0);
            statistics.setPassingRate(0.0);
            statistics.setGradeDistribution(new HashMap<>());
            return statistics;
        }
        
        // Average
        double sum = scores.stream().mapToDouble(Double::doubleValue).sum();
        double average = sum / totalGrades;
        statistics.setAverageScore(average);
        
        // Median
        Collections.sort(scores);
        double median;
        if (totalGrades % 2 == 0) {
            median = (scores.get(totalGrades / 2 - 1) + scores.get(totalGrades / 2)) / 2.0;
        } else {
            median = scores.get(totalGrades / 2);
        }
        statistics.setMedianScore(median);
        
        // Min and Max
        statistics.setMinScore(scores.get(0));
        statistics.setMaxScore(scores.get(totalGrades - 1));
        
        // Standard Deviation
        double variance = scores.stream()
                .mapToDouble(score -> Math.pow(score - average, 2))
                .sum() / totalGrades;
        statistics.setStandardDeviation(Math.sqrt(variance));
        
        // Passing/Failing counts
        int passingCount = (int) scores.stream()
                .filter(score -> score >= PASSING_THRESHOLD)
                .count();
        int failingCount = totalGrades - passingCount;
        statistics.setPassingGrades(passingCount);
        statistics.setFailingGrades(failingCount);
        statistics.setPassingRate((double) passingCount / totalGrades * 100.0);
        
        // Grade distribution (A, B, C, D, F)
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("A", (int) scores.stream().filter(score -> score >= A_THRESHOLD).count());
        distribution.put("B", (int) scores.stream().filter(score -> score >= B_THRESHOLD && score < A_THRESHOLD).count());
        distribution.put("C", (int) scores.stream().filter(score -> score >= C_THRESHOLD && score < B_THRESHOLD).count());
        distribution.put("D", (int) scores.stream().filter(score -> score >= D_THRESHOLD && score < C_THRESHOLD).count());
        distribution.put("F", (int) scores.stream().filter(score -> score < D_THRESHOLD).count());
        statistics.setGradeDistribution(distribution);
        
        return statistics;
    }
    
    /**
     * Calculate subject averages from a list of grades
     */
    private Map<String, Double> calculateSubjectAverages(List<Grade> grades) {
        return grades.stream()
                .filter(grade -> grade.getScore() != null && grade.getSubject() != null)
                .collect(Collectors.groupingBy(
                        grade -> grade.getSubject().getName(),
                        Collectors.averagingDouble(Grade::getScore)
                ));
    }
    
    /**
     * Calculate top student averages from a list of grades
     */
    private Map<String, Double> calculateTopStudentAverages(List<Grade> grades) {
        Map<Student, List<Grade>> gradesByStudent = grades.stream()
                .filter(grade -> grade.getScore() != null && grade.getStudent() != null)
                .collect(Collectors.groupingBy(Grade::getStudent));
        
        // Calculate average for each student
        Map<Student, Double> studentAverages = new HashMap<>();
        for (Map.Entry<Student, List<Grade>> entry : gradesByStudent.entrySet()) {
            Student student = entry.getKey();
            List<Grade> studentGrades = entry.getValue();
            double average = studentGrades.stream()
                    .mapToDouble(Grade::getScore)
                    .average()
                    .orElse(0.0);
            studentAverages.put(student, average);
        }
        
        // Get top students
        return studentAverages.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(MAX_TOP_STUDENTS)
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getFirstName() + " " + entry.getKey().getLastName(),
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        HashMap::new
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public StatisticsDto calculateStudentStatistics(Long studentId, Long semesterId) {
        logger.info("Calculating statistics for student ID: {} and semester ID: {}", studentId, semesterId);
        
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with ID: " + studentId));
        
        List<Grade> grades;
        String semesterName = null;
        
        if (semesterId != null) {
            Semester semester = semesterRepository.findById(semesterId)
                    .orElseThrow(() -> new EntityNotFoundException("Semester not found with ID: " + semesterId));
            grades = gradeRepository.findByStudentAndSemester(student, semester);
            semesterName = semester.getName();
        } else {
            grades = gradeRepository.findByStudent(student);
        }
        
        StatisticsDto statistics = calculateBasicStatistics(
                grades, 
                "student", 
                studentId, 
                student.getFirstName() + " " + student.getLastName(),
                semesterId,
                semesterName
        );
        
        // Add subject averages
        statistics.setSubjectAverages(calculateSubjectAverages(grades));
        
        // Count unique subjects
        statistics.setTotalSubjects((int) grades.stream()
                .map(Grade::getSubject)
                .distinct()
                .count());
        
        // Only one student
        statistics.setTotalStudents(1);
        
        return statistics;
    }

    @Override
    @Transactional(readOnly = true)
    public StatisticsDto calculateSubjectStatistics(Long subjectId, Long semesterId) {
        logger.info("Calculating statistics for subject ID: {} and semester ID: {}", subjectId, semesterId);
        
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new EntityNotFoundException("Subject not found with ID: " + subjectId));
        
        List<Grade> grades;
        String semesterName = null;
        
        if (semesterId != null) {
            Semester semester = semesterRepository.findById(semesterId)
                    .orElseThrow(() -> new EntityNotFoundException("Semester not found with ID: " + semesterId));
            grades = gradeRepository.findBySubjectAndSemester(subject, semester);
            semesterName = semester.getName();
        } else {
            grades = gradeRepository.findBySubject(subject);
        }
        
        StatisticsDto statistics = calculateBasicStatistics(
                grades, 
                "subject", 
                subjectId, 
                subject.getName(),
                semesterId,
                semesterName
        );
        
        // Add top student averages
        statistics.setTopStudentAverages(calculateTopStudentAverages(grades));
        
        // Count unique students
        statistics.setTotalStudents((int) grades.stream()
                .map(Grade::getStudent)
                .distinct()
                .count());
        
        // Only one subject
        statistics.setTotalSubjects(1);
        
        return statistics;
    }

    @Override
    @Transactional(readOnly = true)
    public StatisticsDto calculateClassStatistics(Long classSectionId) {
        logger.info("Calculating statistics for class section ID: {}", classSectionId);
        
        ClassSection classSection = classSectionRepository.findById(classSectionId)
                .orElseThrow(() -> new EntityNotFoundException("Class section not found with ID: " + classSectionId));
        
        Subject subject = classSection.getSubject();
        Semester semester = classSection.getSemester();
        List<Student> students = new ArrayList<>(classSection.getStudents());
        
        if (students.isEmpty()) {
            logger.warn("No students found in class with ID: {}", classSectionId);
            return new StatisticsDto();
        }
        
        // Get all grades for these students in this subject and semester
        List<Grade> grades = gradeRepository.findByStudentInAndSubjectAndSemester(
                students, subject, semester);
        
        String className = subject.getName() + " (" + semester.getName() + ")";
        
        StatisticsDto statistics = calculateBasicStatistics(
                grades, 
                "class", 
                classSectionId, 
                className,
                semester.getId(),
                semester.getName()
        );
        
        // Add top student averages
        statistics.setTopStudentAverages(calculateTopStudentAverages(grades));
        
        // Count students and subjects
        statistics.setTotalStudents(students.size());
        statistics.setTotalSubjects(1);
        
        return statistics;
    }

    @Override
    @Transactional(readOnly = true)
    public StatisticsDto calculateSemesterStatistics(Long semesterId) {
        logger.info("Calculating statistics for semester ID: {}", semesterId);
        
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new EntityNotFoundException("Semester not found with ID: " + semesterId));
        
        List<Grade> grades = gradeRepository.findBySemester(semester);
        
        StatisticsDto statistics = calculateBasicStatistics(
                grades, 
                "semester", 
                semesterId, 
                semester.getName(),
                semesterId,
                semester.getName()
        );
        
        // Add subject averages
        statistics.setSubjectAverages(calculateSubjectAverages(grades));
        
        // Add top student averages
        statistics.setTopStudentAverages(calculateTopStudentAverages(grades));
        
        // Count unique students and subjects
        statistics.setTotalStudents((int) grades.stream()
                .map(Grade::getStudent)
                .distinct()
                .count());
        
        statistics.setTotalSubjects((int) grades.stream()
                .map(Grade::getSubject)
                .distinct()
                .count());
        
        return statistics;
    }

    @Override
    @Transactional(readOnly = true)
    public StatisticsDto calculateOverallStatistics() {
        logger.info("Calculating overall statistics");
        
        List<Grade> grades = gradeRepository.findAll();
        
        StatisticsDto statistics = calculateBasicStatistics(
                grades, 
                "overall", 
                null, 
                "Overall System Statistics",
                null,
                null
        );
        
        // Add subject averages
        statistics.setSubjectAverages(calculateSubjectAverages(grades));
        
        // Add top student averages
        statistics.setTopStudentAverages(calculateTopStudentAverages(grades));
        
        // Count unique students and subjects
        statistics.setTotalStudents((int) grades.stream()
                .map(Grade::getStudent)
                .distinct()
                .count());
        
        statistics.setTotalSubjects((int) grades.stream()
                .map(Grade::getSubject)
                .distinct()
                .count());
        
        return statistics;
    }
}
