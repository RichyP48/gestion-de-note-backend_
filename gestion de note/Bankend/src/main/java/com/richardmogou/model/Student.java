package com.richardmogou.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "students")
@Data // Lombok
@EqualsAndHashCode(callSuper = true, exclude = {"enrolledClasses"}) // Exclude enrolledClasses to prevent circular reference
@NoArgsConstructor // Lombok
public class Student extends User {

    // Student-specific fields
    @ManyToMany(mappedBy = "students")
    private Set<ClassSection> enrolledClasses = new HashSet<>();

    // Constructor calling the superclass constructor
    public Student(String username, String password, String firstName, String lastName, String email) {
        super(username, password, firstName, lastName, email, Role.STUDENT); // Set role automatically
    }
}