package com.richardmogou.StageRichy.repository; // Standard package

import com.richardmogou.StageRichy.model.Grade;
import com.richardmogou.StageRichy.model.Semester;
import com.richardmogou.StageRichy.model.Student;
import com.richardmogou.StageRichy.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    /**
     * Finds all grades for a specific student.
     *
     * @param student The student entity.
     * @return A list of grades for the given student.
     */
    List<Grade> findByStudent(Student student);

    /**
     * Finds all grades for a specific subject.
     *
     * @param subject The subject entity.
     * @return A list of grades for the given subject.
     */
    List<Grade> findBySubject(Subject subject);

    /**
     * Finds all grades for a specific student and subject.
     *
     * @param student The student entity.
     * @param subject The subject entity.
     * @return A list of grades for the given student and subject combination.
     */
    List<Grade> findByStudentAndSubject(Student student, Subject subject);

    /**
     * Finds all grades for a specific semester.
     *
     * @param semester The semester entity.
     * @return A list of grades for the given semester.
     */
    List<Grade> findBySemester(Semester semester);

    /**
     * Finds all grades for a specific student and semester.
     *
     * @param student The student entity.
     * @param semester The semester entity.
     * @return A list of grades for the given student and semester combination.
     */
    List<Grade> findByStudentAndSemester(Student student, Semester semester);
    
    /**
     * Finds all grades for a specific subject and semester.
     *
     * @param subject The subject entity.
     * @param semester The semester entity.
     * @return A list of grades for the given subject and semester combination.
     */
    List<Grade> findBySubjectAndSemester(Subject subject, Semester semester);
    
    /**
     * Finds all grades for a specific student, subject, and semester.
     *
     * @param student The student entity.
     * @param subject The subject entity.
     * @param semester The semester entity.
     * @return A list of grades for the given student, subject, and semester combination.
     */
    List<Grade> findByStudentAndSubjectAndSemester(Student student, Subject subject, Semester semester);
    
    /**
     * Finds all grades for a list of students, a specific subject, and semester.
     *
     * @param students The list of student entities.
     * @param subject The subject entity.
     * @param semester The semester entity.
     * @return A list of grades for the given students, subject, and semester combination.
     */
    List<Grade> findByStudentInAndSubjectAndSemester(List<Student> students, Subject subject, Semester semester);

}