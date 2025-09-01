package com.richardmogou.StageRichy.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "teachers")
@Data // Lombok
@EqualsAndHashCode(callSuper = true, exclude = {"classesTaught"}) // Exclude classesTaught to prevent circular reference
@NoArgsConstructor // Lombok
public class Teacher extends User {

    // Teacher-specific fields
    @OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY)
    private Set<ClassSection> classesTaught = new HashSet<>();

    // Constructor calling the superclass constructor
    public Teacher(String username, String password, String firstName, String lastName, String email) {
        super(username, password, firstName, lastName, email, Role.TEACHER); // Set role automatically
    }
}