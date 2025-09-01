package com.richardmogou.StageRichy.dto; // Standard package

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectRequestDto {

    @NotBlank(message = "Subject name cannot be blank")
    @Size(max = 100, message = "Subject name cannot exceed 100 characters")
    private String name;

    @NotNull(message = "Coefficient cannot be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "Coefficient must be non-negative")
    private Double coefficient;
}