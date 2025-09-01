package com.richardmogou.StageRichy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassSectionDto {
    private Long id;
    private String name;
    
    // Related entities
    private Long subjectId;
    private String subjectName;
    
    private Long semesterId;
    private String semesterName;
    
    private Long teacherId;
    private String teacherUsername;
    private String teacherFullName;
    
    // Student information
    private List<StudentDto> enrolledStudents;
    private int enrollmentCount;
}
