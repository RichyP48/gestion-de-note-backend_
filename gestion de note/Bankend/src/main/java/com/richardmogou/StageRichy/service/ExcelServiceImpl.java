package com.richardmogou.StageRichy.service;

import com.richardmogou.StageRichy.model.*;
import com.richardmogou.StageRichy.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelServiceImpl implements ExcelService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String[] HEADER_COLUMNS = {"Student ID", "Student Name", "Subject", "Score", "Date Assigned", "Semester", "Comments"};

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private ClassSectionRepository classSectionRepository;

    /**
     * Helper method to create workbook with header styles
     */
    private Workbook createWorkbook(String sheetName) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);
        
        // Create header row
        Row headerRow = sheet.createRow(0);
        
        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.BLUE.getIndex());
        headerStyle.setFont(font);
        
        // Create header cells
        for (int i = 0; i < HEADER_COLUMNS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(HEADER_COLUMNS[i]);
            cell.setCellStyle(headerStyle);
            sheet.autoSizeColumn(i);
        }
        
        return workbook;
    }
    
    /**
     * Helper method to add grade data to a workbook
     */
    private void addGradeDataToWorkbook(Workbook workbook, List<Grade> grades) {
        Sheet sheet = workbook.getSheetAt(0);
        CreationHelper createHelper = workbook.getCreationHelper();
        
        // Create date cell style
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd"));
        
        // Add data rows
        int rowNum = 1;
        for (Grade grade : grades) {
            Row row = sheet.createRow(rowNum++);
            
            Student student = grade.getStudent();
            Subject subject = grade.getSubject();
            Semester semester = grade.getSemester();
            
            row.createCell(0).setCellValue(student.getId());
            row.createCell(1).setCellValue(student.getFirstName() + " " + student.getLastName());
            row.createCell(2).setCellValue(subject != null ? subject.getName() : "N/A");
            row.createCell(3).setCellValue(grade.getScore() != null ? grade.getScore() : 0.0);
            
            Cell dateCell = row.createCell(4);
            if (grade.getDateAssigned() != null) {
                dateCell.setCellValue(DATE_FORMATTER.format(grade.getDateAssigned()));
            } else {
                dateCell.setCellValue("N/A");
            }
            
            row.createCell(5).setCellValue(semester != null ? semester.getName() : "N/A");
            row.createCell(6).setCellValue(grade.getComments() != null ? grade.getComments() : "");
        }
        
        // Auto-size columns for better readability
        for (int i = 0; i < HEADER_COLUMNS.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    /**
     * Helper method to write workbook to ByteArrayInputStream
     */
    private ByteArrayInputStream workbookToByteArrayInputStream(Workbook workbook) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return new ByteArrayInputStream(out.toByteArray());
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream generateStudentGradesExcel(Long studentId) throws IOException {
        logger.info("Generating Excel report for student ID: {}", studentId);
        
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with ID: " + studentId));
        
        List<Grade> grades = gradeRepository.findByStudent(student);
        
        if (grades.isEmpty()) {
            logger.warn("No grades found for student ID: {}", studentId);
        }
        
        Workbook workbook = createWorkbook("Student Grades - " + student.getUsername());
        addGradeDataToWorkbook(workbook, grades);
        
        return workbookToByteArrayInputStream(workbook);
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream generateClassGradesExcel(Long classSectionId) throws IOException {
        logger.info("Generating Excel report for class section ID: {}", classSectionId);
        
        ClassSection classSection = classSectionRepository.findById(classSectionId)
                .orElseThrow(() -> new EntityNotFoundException("Class section not found with ID: " + classSectionId));
        
        // Get all students in this class
        List<Student> students = classSection.getStudents().stream().toList();
        
        if (students.isEmpty()) {
            logger.warn("No students found in class section ID: {}", classSectionId);
            throw new IllegalStateException("No students enrolled in class with ID: " + classSectionId);
        }
        
        // Get all grades for these students in this class's subject and semester
        List<Grade> grades = gradeRepository.findByStudentInAndSubjectAndSemester(
                students, 
                classSection.getSubject(), 
                classSection.getSemester());
        
        if (grades.isEmpty()) {
            logger.warn("No grades found for class section ID: {}", classSectionId);
        }
        
        String sheetName = "Class Grades - " + classSection.getSubject().getName();
        Workbook workbook = createWorkbook(sheetName);
        addGradeDataToWorkbook(workbook, grades);
        
        return workbookToByteArrayInputStream(workbook);
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream generateSubjectGradesExcel(Long subjectId, Long semesterId) throws IOException {
        logger.info("Generating Excel report for subject ID: {} and semester ID: {}", subjectId, semesterId);
        
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new EntityNotFoundException("Subject not found with ID: " + subjectId));
        
        List<Grade> grades;
        String sheetName = "Subject Grades - " + subject.getName();
        
        if (semesterId != null) {
            Semester semester = semesterRepository.findById(semesterId)
                    .orElseThrow(() -> new EntityNotFoundException("Semester not found with ID: " + semesterId));
            
            grades = gradeRepository.findBySubjectAndSemester(subject, semester);
            sheetName += " (" + semester.getName() + ")";
        } else {
            grades = gradeRepository.findBySubject(subject);
        }
        
        if (grades.isEmpty()) {
            logger.warn("No grades found for subject ID: {} and semester ID: {}", subjectId, semesterId);
        }
        
        Workbook workbook = createWorkbook(sheetName);
        addGradeDataToWorkbook(workbook, grades);
        
        return workbookToByteArrayInputStream(workbook);
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream generateSemesterGradesExcel(Long semesterId) throws IOException {
        logger.info("Generating Excel report for semester ID: {}", semesterId);
        
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new EntityNotFoundException("Semester not found with ID: " + semesterId));
        
        List<Grade> grades = gradeRepository.findBySemester(semester);
        
        if (grades.isEmpty()) {
            logger.warn("No grades found for semester ID: {}", semesterId);
        }
        
        String sheetName = "Semester Grades - " + semester.getName();
        Workbook workbook = createWorkbook(sheetName);
        addGradeDataToWorkbook(workbook, grades);
        
        return workbookToByteArrayInputStream(workbook);
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream generateAllGradesExcel() throws IOException {
        logger.info("Generating Excel report for all grades");
        
        List<Grade> grades = gradeRepository.findAll();
        
        if (grades.isEmpty()) {
            logger.warn("No grades found in the system");
        }
        
        Workbook workbook = createWorkbook("All Grades");
        addGradeDataToWorkbook(workbook, grades);
        
        return workbookToByteArrayInputStream(workbook);
    }
}
