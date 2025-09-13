package com.richardmogou.repository;


import com.richardmogou.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassSectionRepository extends JpaRepository<ClassSection, Long> {

    /**
     * Finds all classes taught by a specific teacher.
     *
     * @param teacher The teacher entity.
     * @return A list of classes taught by the given teacher.
     */
    List<ClassSection> findByTeacher(Teacher teacher);

    /**
     * Finds all classes for a specific subject.
     *
     * @param subject The subject entity.
     * @return A list of classes for the given subject.
     */
    List<ClassSection> findBySubject(Subject subject);

    /**
     * Finds all classes for a specific semester.
     *
     * @param semester The semester entity.
     * @return A list of classes for the given semester.
     */
    List<ClassSection> findBySemester(Semester semester);

    /**
     * Finds all classes taught by a specific teacher in a specific semester.
     *
     * @param teacher The teacher entity.
     * @param semester The semester entity.
     * @return A list of classes taught by the given teacher in the given semester.
     */
    List<ClassSection> findByTeacherAndSemester(Teacher teacher, Semester semester);

    /**
     * Finds all classes for a specific subject in a specific semester.
     *
     * @param subject The subject entity.
     * @param semester The semester entity.
     * @return A list of classes for the given subject in the given semester.
     */
    List<ClassSection> findBySubjectAndSemester(Subject subject, Semester semester);

    /**
     * Finds all classes taught by a specific teacher for a specific subject.
     *
     * @param teacher The teacher entity.
     * @param subject The subject entity.
     * @return A list of classes taught by the given teacher for the given subject.
     */
    List<ClassSection> findByTeacherAndSubject(Teacher teacher, Subject subject);

    /**
     * Finds all classes taught by a specific teacher for a specific subject in a specific semester.
     *
     * @param teacher The teacher entity.
     * @param subject The subject entity.
     * @param semester The semester entity.
     * @return A list of classes taught by the given teacher for the given subject in the given semester.
     */
    List<ClassSection> findByTeacherAndSubjectAndSemester(Teacher teacher, Subject subject, Semester semester);

    /**
     * Finds all classes that a specific student is enrolled in.
     *
     * @param student The student entity.
     * @return A list of classes that the given student is enrolled in.
     */
    List<ClassSection> findByStudentsContaining(Student student);

    /**
     * Finds all classes that a specific student is enrolled in for a specific semester.
     *
     * @param student The student entity.
     * @param semester The semester entity.
     * @return A list of classes that the given student is enrolled in for the given semester.
     */
    List<ClassSection> findByStudentsContainingAndSemester(Student student, Semester semester);
}
