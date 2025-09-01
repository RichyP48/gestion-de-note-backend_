package com.richardmogou.StageRichy.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public interface ExcelService {
    
    /**
     * Generates an Excel file containing grades for a specific student
     * 
     * @param studentId The ID of the student
     * @return ByteArrayInputStream containing the Excel file data
     * @throws IOException If an error occurs during Excel generation
     */
    ByteArrayInputStream generateStudentGradesExcel(Long studentId) throws IOException;
    
    /**
     * Generates an Excel file containing grades for a specific class section
     * 
     * @param classSectionId The ID of the class section
     * @return ByteArrayInputStream containing the Excel file data
     * @throws IOException If an error occurs during Excel generation
     */
    ByteArrayInputStream generateClassGradesExcel(Long classSectionId) throws IOException;
    
    /**
     * Generates an Excel file containing grades for a specific subject
     * 
     * @param subjectId The ID of the subject
     * @param semesterId Optional semester ID to filter grades
     * @return ByteArrayInputStream containing the Excel file data
     * @throws IOException If an error occurs during Excel generation
     */
    ByteArrayInputStream generateSubjectGradesExcel(Long subjectId, Long semesterId) throws IOException;
    
    /**
     * Generates an Excel file containing grades for a specific semester
     * 
     * @param semesterId The ID of the semester
     * @return ByteArrayInputStream containing the Excel file data
     * @throws IOException If an error occurs during Excel generation
     */
    ByteArrayInputStream generateSemesterGradesExcel(Long semesterId) throws IOException;
    
    /**
     * Generates an Excel file containing all grades in the system
     * 
     * @return ByteArrayInputStream containing the Excel file data
     * @throws IOException If an error occurs during Excel generation
     */
    ByteArrayInputStream generateAllGradesExcel() throws IOException;
}
