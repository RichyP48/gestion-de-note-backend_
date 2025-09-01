package com.richardmogou.StageRichy.repository; // Standard package

import com.richardmogou.StageRichy.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // Find a student by their username (inherited from User)
    Optional<Student> findByUsername(String username);

    // Find a student by their email (inherited from User)
    Optional<Student> findByEmail(String email);

    // Add other student-specific query methods here if needed
    // Example:
    // List<Student> findByEnrollmentDateAfter(LocalDate date);
}