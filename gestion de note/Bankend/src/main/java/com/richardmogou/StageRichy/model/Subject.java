package com.richardmogou.StageRichy.model; // Standard package

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subjects")
@Data
@NoArgsConstructor
@AllArgsConstructor // Add this to explicitly generate all-args constructor too
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Subject name cannot be blank")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @NotNull(message = "Coefficient cannot be null")
    @Column(nullable = false)
    private Double coefficient = 1.0; // Default coefficient/weighting

    // Relationships (will be mapped later if needed)
    // @OneToMany(mappedBy = "subject")
    // private Set<Grade> grades = new HashSet<>();

    // @ManyToMany(mappedBy = "subjectsTaught")
    // private Set<Teacher> teachers = new HashSet<>();

    public Subject(String name, Double coefficient) {
        this.name = name;
        this.coefficient = coefficient;
    }
}