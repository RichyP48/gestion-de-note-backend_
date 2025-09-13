package com.richardmogou.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDto {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    
    // Helper method to get full name
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
