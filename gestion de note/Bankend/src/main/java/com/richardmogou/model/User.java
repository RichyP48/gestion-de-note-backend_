package com.richardmogou.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Entity // Make User a concrete entity
@Table(name = "users") // Explicitly name the base table
@Inheritance(strategy = InheritanceType.JOINED) // Use JOINED inheritance strategy
@Data // Lombok annotation for getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok annotation for no-args constructor
public abstract class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @NotBlank(message = "Password cannot be blank")
    // Password length validation might be better handled during hashing/encoding
    @Column(nullable = false)
    private String password; // Store hashed password

    @NotBlank(message = "First name cannot be blank")
    @Column(nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Column(nullable = false, length = 50)
    private String lastName;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email cannot be blank")
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @NotNull(message = "Role cannot be null")
    @Enumerated(EnumType.STRING) // Store role as string (STUDENT, TEACHER, ADMIN)
    @Column(nullable = false, length = 10)
    private Role role;

    // UserDetails implementation methods
    // These are needed for Spring Security integration

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Return a collection containing the user's role
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    // Account status methods (can be customized later if needed)
    @Override
    public boolean isAccountNonExpired() {
        return true; // Default to true
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Default to true
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Default to true
    }

    @Override
    public boolean isEnabled() {
        return true; // Default to true
    }

    // Constructor for common fields (optional, Lombok handles NoArgsConstructor)
    public User(String username, String password, String firstName, String lastName, String email, Role role) {
        this.username = username;
        this.password = password; // Remember to hash this before saving!
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
    }
}