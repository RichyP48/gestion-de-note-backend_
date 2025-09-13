package com.richardmogou.repository; // Standard package

import com.richardmogou.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    /**
     * Finds a subject by its name (case-insensitive).
     *
     * @param name The name of the subject to search for.
     * @return An Optional containing the Subject if found, otherwise empty.
     */
    Optional<Subject> findByNameIgnoreCase(String name);

    /**
     * Checks if a subject exists with the given name (case-insensitive).
     *
     * @param name The name to check.
     * @return true if a subject with the name exists, false otherwise.
     */
    boolean existsByNameIgnoreCase(String name);

}