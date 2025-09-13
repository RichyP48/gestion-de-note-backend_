package com.richardmogou.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public interface PdfService {

    /**
     * Generates a PDF grade report for a single student.
     * Includes grades for all subjects, potentially grouped by semester if implemented.
     *
     * @param studentId The ID of the student for whom to generate the report.
     * @return A ByteArrayInputStream containing the generated PDF data.
     * @throws IOException If an error occurs during PDF generation.
     * @throws jakarta.persistence.EntityNotFoundException If the student is not found.
     */
    ByteArrayInputStream generateStudentGradeReport(Long studentId) throws IOException;

    /**
     * Generates an official transcript PDF for a single student.
     * This might include more details than a simple report (e.g., overall average, school info).
     *
     * @param studentId The ID of the student for whom to generate the transcript.
     * @return A ByteArrayInputStream containing the generated PDF data.
     * @throws IOException If an error occurs during PDF generation.
     * @throws jakarta.persistence.EntityNotFoundException If the student is not found.
     */
    ByteArrayInputStream generateStudentTranscript(Long studentId) throws IOException;
    
    /**
     * Generates PDF grade reports for multiple students and returns them as a ZIP archive.
     *
     * @param studentIds List of student IDs for whom to generate reports.
     * @return A ByteArrayInputStream containing the generated ZIP data with PDFs.
     * @throws IOException If an error occurs during PDF or ZIP generation.
     * @throws jakarta.persistence.EntityNotFoundException If any student is not found.
     */
    ByteArrayInputStream generateBulkStudentReports(List<Long> studentIds) throws IOException;
    
    /**
     * Generates PDF grade reports for all students in a specific class and returns them as a ZIP archive.
     *
     * @param classSectionId The ID of the class section.
     * @return A ByteArrayInputStream containing the generated ZIP data with PDFs.
     * @throws IOException If an error occurs during PDF or ZIP generation.
     * @throws jakarta.persistence.EntityNotFoundException If the class section is not found.
     */
    ByteArrayInputStream generateClassReports(Long classSectionId) throws IOException;
    
    /**
     * Generates PDF grade reports for all students in a specific semester and returns them as a ZIP archive.
     *
     * @param semesterId The ID of the semester.
     * @return A ByteArrayInputStream containing the generated ZIP data with PDFs.
     * @throws IOException If an error occurs during PDF or ZIP generation.
     * @throws jakarta.persistence.EntityNotFoundException If the semester is not found.
     */
    ByteArrayInputStream generateSemesterReports(Long semesterId) throws IOException;
}