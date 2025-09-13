package com.richardmogou.repository; // Standard package

import com.richardmogou.model.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Long> {

    /**
     * Finds a semester by its name (case-insensitive).
     *
     * @param name The name of the semester.
     * @return An Optional containing the Semester if found, otherwise empty.
     */
    Optional<Semester> findByNameIgnoreCase(String name);

    /**
     * Checks if a semester exists with the given name (case-insensitive).
     *
     * @param name The name to check.
     * @return true if a semester with the name exists, false otherwise.
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Finds semesters that overlap with a given date range.
     * Useful for finding the current semester or semesters within a specific period.
     *
     * @param date The date to check for overlap.
     * @return A list of semesters where the given date falls between the start and end dates (inclusive).
     */
    List<Semester> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate date, LocalDate date2);

}