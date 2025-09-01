package com.richardmogou.StageRichy.model; // Standard package

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "semesters")
@Data
@NoArgsConstructor
public class Semester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Semester name cannot be blank")
    @Column(nullable = false, unique = true, length = 50) // e.g., "Fall 2024", "Spring 2025"
    private String name;

    @NotNull(message = "Start date cannot be null")
    @Column(nullable = false)
    private LocalDate startDate;

    @NotNull(message = "End date cannot be null")
    @Column(nullable = false)
    private LocalDate endDate;

    // Optional: Relationship back to Grades (if needed for semester-specific queries)
    // @OneToMany(mappedBy = "semester", cascade = CascadeType.ALL, orphanRemoval = true)
    // private Set<Grade> grades = new HashSet<>();

    public Semester(String name, LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Basic validation: Ensure end date is after start date
    @PrePersist
    @PreUpdate
    private void validateDates() {
        if (startDate != null && endDate != null && !endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("Semester end date must be after start date.");
        }
    }
}