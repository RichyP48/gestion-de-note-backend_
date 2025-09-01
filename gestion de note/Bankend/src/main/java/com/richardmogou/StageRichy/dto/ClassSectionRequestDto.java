package com.richardmogou.StageRichy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassSectionRequestDto {
    
    @NotBlank(message = "Class name cannot be blank")
    private String name;
    
    @NotNull(message = "Subject ID cannot be null")
    private Long subjectId;
    
    @NotNull(message = "Semester ID cannot be null")
    private Long semesterId;
    
    private Long teacherId; // Optional, can be assigned later
    
    private List<Long> studentIds; // Optional, students can be enrolled later
}
