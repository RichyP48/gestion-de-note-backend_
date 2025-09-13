package com.richardmogou.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "class_sections")
@Data
@EqualsAndHashCode(exclude = {"teacher", "students"}) // Exclude bidirectional relationships to prevent circular references
@NoArgsConstructor
public class ClassSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Class name cannot be blank")
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull(message = "Subject cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @NotNull(message = "Semester cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    @ManyToMany
    @JoinTable(
        name = "class_student",
        joinColumns = @JoinColumn(name = "class_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private Set<Student> students = new HashSet<>();

    // Constructor with essential fields
    public ClassSection(String name, Subject subject, Semester semester) {
        this.name = name;
        this.subject = subject;
        this.semester = semester;
    }

    // Constructor with all fields
    public ClassSection(String name, Subject subject, Semester semester, Teacher teacher) {
        this.name = name;
        this.subject = subject;
        this.semester = semester;
        this.teacher = teacher;
    }

    // Helper methods to manage students
    public void addStudent(Student student) {
        students.add(student);
    }

    public void removeStudent(Student student) {
        students.remove(student);
    }
}
