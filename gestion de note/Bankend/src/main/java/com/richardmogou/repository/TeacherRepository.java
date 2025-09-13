package com.richardmogou.repository; // Standard package

import com.richardmogou.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    // Find a teacher by their username (inherited from User)
    Optional<Teacher> findByUsername(String username);

    // Find a teacher by their email (inherited from User)
    Optional<Teacher> findByEmail(String email);

    // Add other teacher-specific query methods here if needed
    // Example:
    // List<Teacher> findByDepartment(String department);
}