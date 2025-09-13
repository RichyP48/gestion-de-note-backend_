package com.richardmogou.model; // Standard package

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data // Lombok
@Table(name = "admins")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor // Lombok
public class Admin extends User {

    // Add admin-specific fields here later if needed
    // For example:
    // private String officeLocation;

    // Constructor calling the superclass constructor
    public Admin(String username, String password, String firstName, String lastName, String email) {
        super(username, password, firstName, lastName, email, Role.ADMIN); // Set role automatically
    }
}