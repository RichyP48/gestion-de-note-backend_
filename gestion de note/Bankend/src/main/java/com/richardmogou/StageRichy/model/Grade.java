package com.richardmogou.StageRichy.model; // Standard package

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "grades")
@Data
@NoArgsConstructor
@AllArgsConstructor // Add this to explicitly generate all-args constructor too
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Score cannot be null")
    @Min(value = 0, message = "Score must be at least 0")
    @Max(value = 100, message = "Score must be at most 100") // Assuming a 0-100 scale, adjust if needed
    @Column(nullable = false)
    private Double score;

    @Column(columnDefinition = "TEXT") // Allow longer comments
    private String comments; // Teacher's comments on the submission/grade

    @NotNull(message = "Grade date cannot be null")
    @Column(nullable = false)
    private LocalDate dateAssigned = LocalDate.now(); // Default to current date

    // Relationships
    @NotNull(message = "Student cannot be null")
    @ManyToOne(fetch = FetchType.LAZY) // Lazy fetch is often better for performance
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull(message = "Subject cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    // Link to a specific semester
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id")
    private Semester semester;

    // Constructor
    public Grade(Double score, String comments, Student student, Subject subject) {
        this.score = score;
        this.comments = comments;
        this.student = student;
        this.subject = subject;
    }
    
    // Constructor with semester
    public Grade(Double score, String comments, Student student, Subject subject, Semester semester) {
        this.score = score;
        this.comments = comments;
        this.student = student;
        this.subject = subject;
        this.semester = semester;
    }
}