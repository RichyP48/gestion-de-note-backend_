package com.richardmogou.service;

import com.richardmogou.model.*;
import com.richardmogou.repository.ClassSectionRepository;
import com.richardmogou.repository.GradeRepository;
import com.richardmogou.repository.SemesterRepository;
import com.richardmogou.repository.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    private void addBackgroundImage(PDDocument document, PDPageContentStream contentStream, PDPage page) throws IOException {
        try (InputStream imageStream = getClass().getResourceAsStream("/images/bgs.jpg")) {
            if (imageStream == null) {
                throw new IOException("Background image not found in resources.");
            }
            PDImageXObject background = PDImageXObject.createFromByteArray(document, imageStream.readAllBytes(), "bg.PNG");

            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            contentStream.drawImage(background, 0, 0, pageWidth, pageHeight);
        }
    }

    private void addText(PDPageContentStream contentStream, float x, float y, String text, PDType1Font font, float fontSize) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text != null ? text : ""); // Handle null text
        contentStream.endText();
    }

    private float addHeader(PDPageContentStream contentStream, PDPage page, float yPosition, String title) throws IOException {
        PDType1Font fontBold = PDType1Font.HELVETICA_BOLD;
        addText(contentStream, 50, yPosition, title, fontBold, 16);
        yPosition -= 30; // Space after header
        return yPosition;
    }

    private float addStudentInfo(PDPageContentStream contentStream, PDPage page, float yPosition, Student student) throws IOException {
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

    private float addGradesTable(PDDocument document, PDPageContentStream contentStream, PDPage page, float y, List<Grade> grades) throws IOException {
        float margin = 50;
        float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
        float rowHeight = 20;
        float cellPadding = 3;

        PDType1Font font = PDType1Font.HELVETICA;
        PDType1Font fontBold = PDType1Font.HELVETICA_BOLD;

        float[] colWidths = { 150, 60, 100, 200 };
        String[] headers = { "Subject", "Score", "Date Assigned", "Comments" };

        float x = margin;
        y -= rowHeight;
        for (int i = 0; i < headers.length; i++) {
            addText(contentStream, x + cellPadding, y + 5, headers[i], fontBold, 10);
            x += colWidths[i];
        }

        y -= 5;
        contentStream.moveTo(margin, y);
        contentStream.lineTo(margin + tableWidth, y);
        contentStream.stroke();

        y -= rowHeight;
        for (Grade grade : grades) {
            if (y < 50) {
                contentStream.close();
                page = new PDPage();
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                y = page.getMediaBox().getHeight() - 50;
            }

            x = margin;
            String[] rowData = {
                    grade.getSubject() != null ? grade.getSubject().getName() : "N/A",
                    grade.getScore() != null ? String.format("%.2f", grade.getScore()) : "N/A",
                    grade.getDateAssigned() != null ? DATE_FORMATTER.format(grade.getDateAssigned()) : "N/A",
                    grade.getComments() != null ? grade.getComments() : ""
            };

            for (int i = 0; i < rowData.length; i++) {
                String cellText = rowData[i];
                if (i == 3 && cellText.length() > 35) {
                    cellText = cellText.substring(0, 32) + "...";
                }
                addText(contentStream, x + cellPadding, y + 5, cellText, font, 10);
                x += colWidths[i];
            }
            y -= rowHeight;
        }

        return y;
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
            addBackgroundImage(document, contentStream, page);

            float yPosition = page.getMediaBox().getHeight() - 50; // Start near top

            yPosition = addHeader(contentStream, page, yPosition, "Student Grade Report");
            yPosition = addStudentInfo(contentStream, page, yPosition, student);
            yPosition = addGradesTable(document, contentStream, page, yPosition, grades);

            Double overallAverage = calculationService.calculateOverallAverage(studentId);
            if (overallAverage != null && !Double.isNaN(overallAverage)) {
                yPosition -= 30; // Space before average
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

        try (ByteArrayOutputStream zipOut = new ByteArrayOutputStream();
             ZipOutputStream zipStream = new ZipOutputStream(zipOut)) {

            for (Long studentId : studentIds) {
                try {
                    Student student = studentRepository.findById(studentId)
                            .orElseThrow(() -> new EntityNotFoundException("Student not found with ID: " + studentId));

                    ByteArrayInputStream pdfStream = generateStudentGradeReport(studentId);
                    byte[] pdfBytes = pdfStream.readAllBytes();

                    String filename = "grade_report_" + student.getUsername() + "_" + studentId + ".pdf";
                    ZipEntry entry = new ZipEntry(filename);
                    zipStream.putNextEntry(entry);
                    zipStream.write(pdfBytes);
                    zipStream.closeEntry();

                    logger.info("Added report for student {} to bulk ZIP", studentId);
                } catch (EntityNotFoundException e) {
                    logger.warn("Skipping student ID {} in bulk report: {}", studentId, e.getMessage());
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

        ClassSection classSection = classSectionRepository.findById(classSectionId)
                .orElseThrow(() -> new EntityNotFoundException("Class section not found with ID: " + classSectionId));

        Set<Student> students = classSection.getStudents();

        if (students.isEmpty()) {
            logger.warn("No students found in class with ID: {}", classSectionId);
            throw new IllegalStateException("No students enrolled in class with ID: " + classSectionId);
        }

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

        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new EntityNotFoundException("Semester not found with ID: " + semesterId));

        List<Grade> gradesInSemester = gradeRepository.findBySemester(semester);

        if (gradesInSemester.isEmpty()) {
            logger.warn("No grades found in semester with ID: {}", semesterId);
            throw new IllegalStateException("No grades recorded in semester with ID: " + semesterId);
        }

        List<Long> studentIds = gradesInSemester.stream()
                .map(grade -> grade.getStudent().getId())
                .distinct()
                .collect(Collectors.toList());

        logger.info("Generating reports for {} students with grades in semester ID: {}", studentIds.size(), semesterId);
        return generateBulkStudentReports(studentIds);
    }
}
