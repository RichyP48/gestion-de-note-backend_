package com.richardmogou.StageRichy.service;

import com.richardmogou.StageRichy.model.*;
import com.richardmogou.StageRichy.repository.ClassSectionRepository;
import com.richardmogou.StageRichy.repository.GradeRepository;
import com.richardmogou.StageRichy.repository.SemesterRepository;
import com.richardmogou.StageRichy.repository.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class PdfServiceImpl implements PdfService {

    private static final Logger logger = LoggerFactory.getLogger(PdfServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private GradeRepository gradeRepository;
    
    @Autowired
    private ClassSectionRepository classSectionRepository;
    
    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private CalculationService calculationService; // Inject calculation service

    // --- PDF Generation Helper Methods ---

    private void addText(PDPageContentStream contentStream, float x, float y, String text, PDType1Font font, float fontSize) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text != null ? text : ""); // Handle null text
        contentStream.endText();
    }

    private float addHeader(PDPageContentStream contentStream, PDPage page, float yPosition, String title) throws IOException {
        // Use static font instance
        PDType1Font fontBold = PDType1Font.HELVETICA_BOLD;
        addText(contentStream, 50, yPosition, title, fontBold, 16);
        yPosition -= 30; // Space after header
        return yPosition;
    }

     private float addStudentInfo(PDPageContentStream contentStream, PDPage page, float yPosition, Student student) throws IOException {
        // Use static font instances
        PDType1Font font = PDType1Font.HELVETICA;
        PDType1Font fontBold = PDType1Font.HELVETICA_BOLD;
        addText(contentStream, 50, yPosition, "Student Name:", fontBold, 12);
        addText(contentStream, 150, yPosition, student.getFirstName() + " " + student.getLastName(), font, 12);
        yPosition -= 20;
        addText(contentStream, 50, yPosition, "Username:", fontBold, 12);
        addText(contentStream, 150, yPosition, student.getUsername(), font, 12);
        yPosition -= 20;
        addText(contentStream, 50, yPosition, "Email:", fontBold, 12);
        addText(contentStream, 150, yPosition, student.getEmail(), font, 12);
        yPosition -= 30; // Space after info
        return yPosition;
    }

    private float addGradesTable(PDPageContentStream contentStream, PDPage page, float yPosition, List<Grade> grades) throws IOException {
        // Use static font instances
        PDType1Font font = PDType1Font.HELVETICA;
        PDType1Font fontBold = PDType1Font.HELVETICA_BOLD;
        float tableTopY = yPosition;
        float margin = 50;
        float rowHeight = 20;
        float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
        float y = tableTopY;

        // Draw table header
        y -= rowHeight;
        addText(contentStream, margin + 5, y, "Subject", fontBold, 10);
        addText(contentStream, margin + 200, y, "Score", fontBold, 10);
        addText(contentStream, margin + 300, y, "Date Assigned", fontBold, 10);
        addText(contentStream, margin + 400, y, "Comments", fontBold, 10);
        y -= 5; // Line below header
        contentStream.moveTo(margin, y);
        contentStream.lineTo(margin + tableWidth, y);
        contentStream.stroke();
        y -= rowHeight;

        // Draw table rows
        for (Grade grade : grades) {
             if (y < margin) { // Check if new page is needed
                 contentStream.close();
                 page = new PDPage();
                 // TODO: Add page to document (how to access document here?) - Refactor needed
                 contentStream = new PDPageContentStream(new PDDocument(), page); // Incorrect: Need the main document
                 // Reset Y position and potentially redraw header
                 y = page.getMediaBox().getHeight() - margin;
                 // Redraw header logic needed here
                 logger.warn("PDF page break logic needs refactoring to handle document correctly.");
             }

            Subject subject = grade.getSubject();
            addText(contentStream, margin + 5, y, subject != null ? subject.getName() : "N/A", font, 10);
            addText(contentStream, margin + 200, y, grade.getScore() != null ? String.format("%.2f", grade.getScore()) : "N/A", font, 10);
            addText(contentStream, margin + 300, y, grade.getDateAssigned() != null ? DATE_FORMATTER.format(grade.getDateAssigned()) : "N/A", font, 10);
            // Handle potentially long comments (basic truncation shown)
            String comment = grade.getComments();
            if (comment != null && comment.length() > 30) {
                 comment = comment.substring(0, 27) + "...";
            }
            addText(contentStream, margin + 400, y, comment, font, 10);
            y -= rowHeight;
        }
         // Draw table bottom line
        contentStream.moveTo(margin, y + rowHeight); // Back to last row line position
        contentStream.lineTo(margin + tableWidth, y + rowHeight);
        contentStream.stroke();

        return y; // Return the final y position
    }


    // --- Service Method Implementations ---

    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream generateStudentGradeReport(Long studentId) throws IOException {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with ID: " + studentId));
        List<Grade> grades = gradeRepository.findByStudent(student);

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            float yPosition = page.getMediaBox().getHeight() - 50; // Start near top

            // Add content
            yPosition = addHeader(contentStream, page, yPosition, "Student Grade Report");
            yPosition = addStudentInfo(contentStream, page, yPosition, student);
            yPosition = addGradesTable(contentStream, page, yPosition, grades); // Add grades table

            // Add Overall Average (if calculated)
            Double overallAverage = calculationService.calculateOverallAverage(studentId);
            if (overallAverage != null && !Double.isNaN(overallAverage)) {
                 yPosition -= 30; // Space before average
                 // Use static font instance
                 PDType1Font fontBold = PDType1Font.HELVETICA_BOLD;
                 addText(contentStream, 50, yPosition, "Overall Weighted Average:", fontBold, 12);
                 addText(contentStream, 250, yPosition, String.format("%.2f", overallAverage), fontBold, 12);
            }

            contentStream.close();
            document.save(out);
            logger.info("Generated grade report PDF for student ID: {}", studentId);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            logger.error("Error generating PDF grade report for student ID {}: {}", studentId, e.getMessage(), e);
            throw e; // Re-throw exception
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream generateStudentTranscript(Long studentId) throws IOException {
        // For now, make transcript same as report. Can be enhanced later.
        // Enhancements: Add school info, official seals, maybe group by semester, different formatting.
        logger.info("Generating transcript (currently same as report) for student ID: {}", studentId);
        return generateStudentGradeReport(studentId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream generateBulkStudentReports(List<Long> studentIds) throws IOException {
        logger.info("Generating bulk PDF reports for {} students", studentIds.size());
        
        if (studentIds.isEmpty()) {
            throw new IllegalArgumentException("No student IDs provided");
        }
        
        // Create a ZIP file containing all student reports
        try (ByteArrayOutputStream zipOut = new ByteArrayOutputStream();
             ZipOutputStream zipStream = new ZipOutputStream(zipOut)) {
            
            for (Long studentId : studentIds) {
                try {
                    // Check if student exists
                    Student student = studentRepository.findById(studentId)
                            .orElseThrow(() -> new EntityNotFoundException("Student not found with ID: " + studentId));
                    
                    // Generate the PDF for this student
                    ByteArrayInputStream pdfStream = generateStudentGradeReport(studentId);
                    byte[] pdfBytes = pdfStream.readAllBytes();
                    
                    // Add PDF to ZIP with appropriate filename
                    String filename = "grade_report_" + student.getUsername() + "_" + studentId + ".pdf";
                    ZipEntry entry = new ZipEntry(filename);
                    zipStream.putNextEntry(entry);
                    zipStream.write(pdfBytes);
                    zipStream.closeEntry();
                    
                    logger.info("Added report for student {} to bulk ZIP", studentId);
                } catch (EntityNotFoundException e) {
                    logger.warn("Skipping student ID {} in bulk report: {}", studentId, e.getMessage());
                    // Continue with other students rather than failing the entire operation
                }
            }
            
            zipStream.finish();
            zipStream.close();
            
            logger.info("Completed bulk PDF report generation for {} students", studentIds.size());
            return new ByteArrayInputStream(zipOut.toByteArray());
        } catch (IOException e) {
            logger.error("Error generating bulk PDF reports: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream generateClassReports(Long classSectionId) throws IOException {
        logger.info("Generating PDF reports for all students in class ID: {}", classSectionId);
        
        // Find the class and verify it exists
        ClassSection classSection = classSectionRepository.findById(classSectionId)
                .orElseThrow(() -> new EntityNotFoundException("Class section not found with ID: " + classSectionId));
        
        // Get all students enrolled in this class
        Set<Student> students = classSection.getStudents();
        
        if (students.isEmpty()) {
            logger.warn("No students found in class with ID: {}", classSectionId);
            throw new IllegalStateException("No students enrolled in class with ID: " + classSectionId);
        }
        
        // Extract student IDs and use the bulk generation method
        List<Long> studentIds = students.stream()
                .map(Student::getId)
                .collect(Collectors.toList());
        
        logger.info("Generating reports for {} students in class ID: {}", studentIds.size(), classSectionId);
        return generateBulkStudentReports(studentIds);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream generateSemesterReports(Long semesterId) throws IOException {
        logger.info("Generating PDF reports for all students in semester ID: {}", semesterId);
        
        // Find the semester and verify it exists
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new EntityNotFoundException("Semester not found with ID: " + semesterId));
        
        // Find all students who have grades in this semester
        // This is more complex as we need to find unique students with grades in this semester
        List<Grade> gradesInSemester = gradeRepository.findBySemester(semester);
        
        if (gradesInSemester.isEmpty()) {
            logger.warn("No grades found in semester with ID: {}", semesterId);
            throw new IllegalStateException("No grades recorded in semester with ID: " + semesterId);
        }
        
        // Extract unique student IDs from grades
        List<Long> studentIds = gradesInSemester.stream()
                .map(grade -> grade.getStudent().getId())
                .distinct()
                .collect(Collectors.toList());
        
        logger.info("Generating reports for {} students with grades in semester ID: {}", studentIds.size(), semesterId);
        return generateBulkStudentReports(studentIds);
    }
}