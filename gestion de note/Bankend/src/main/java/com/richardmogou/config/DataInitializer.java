package com.richardmogou.config;

import com.richardmogou.model.*;
import com.richardmogou.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired private UserRepository userRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private AdminRepository adminRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private GradeRepository gradeRepository;
    @Autowired private SemesterRepository semesterRepository;
    @Autowired private ClassSectionRepository classSectionRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    @Transactional // Ensure operations run within a transaction
    public void run(String... args) throws Exception {
        logger.info("Checking if initial data needs to be loaded...");

        // Check if data already exists (e.g., by checking for a specific user)
        if (userRepository.existsByUsername("admin")) {
            logger.info("Initial data already exists. Skipping data initialization.");
            return;
        }

        logger.info("Loading initial data...");

        // --- Create Semesters ---
        Semester fall2024 = semesterRepository.save(new Semester("Fall 2024", LocalDate.of(2024, 9, 1), LocalDate.of(2024, 12, 20)));
        Semester spring2025 = semesterRepository.save(new Semester("Spring 2025", LocalDate.of(2025, 1, 15), LocalDate.of(2025, 5, 15)));
        logger.info("Created Semesters: {}, {}", fall2024.getName(), spring2025.getName());

        // --- Create Subjects ---
        Subject math = subjectRepository.save(new Subject("Mathematics", 1.5));
        Subject physics = subjectRepository.save(new Subject("Physics", 1.0));
        Subject chemistry = subjectRepository.save(new Subject("Chemistry", 1.0));
        Subject biology = subjectRepository.save(new Subject("Biology", 1.0));
        Subject history = subjectRepository.save(new Subject("History", 0.5));
        logger.info("Created Subjects: {}, {}, {}, {}, {}", math.getName(), physics.getName(), chemistry.getName(), biology.getName(), history.getName());

        // --- Create Users (with encoded passwords) ---
        String encodedPassword = passwordEncoder.encode("password123"); // Common password for seed data

        adminRepository.save(new Admin("admin", encodedPassword, "Admin", "User", "admin@school.edu"));
        teacherRepository.save(new Teacher("sarahDoctor", passwordEncoder.encode("Doctor123@"), "Sarah", "Mogou", "sarahmogou99@gmail.com"));

        Teacher teacher1 = teacherRepository.save(new Teacher("teacher_jane", encodedPassword, "Jane", "Doe", "jane.doe@school.edu"));
        Teacher teacher2 = teacherRepository.save(new Teacher("teacher_peter", encodedPassword, "Peter", "Jones", "peter.jones@school.edu"));
        Teacher teacher3 = teacherRepository.save(new Teacher("teacher_sarah", encodedPassword, "Sarah", "Williams", "sarah.williams@school.edu"));

        Student student1 = studentRepository.save(new Student("student_john", encodedPassword, "John", "Smith", "john.smith@school.edu"));
        Student student2 = studentRepository.save(new Student("student_lisa", encodedPassword, "Lisa", "Miller", "lisa.miller@school.edu"));
        Student student3 = studentRepository.save(new Student("student_sam", encodedPassword, "Sam", "Brown", "sam.brown@school.edu"));
        Student student4 = studentRepository.save(new Student("student_emma", encodedPassword, "Emma", "Johnson", "emma.johnson@school.edu"));
        Student student5 = studentRepository.save(new Student("student_michael", encodedPassword, "Michael", "Davis", "michael.davis@school.edu"));

        logger.info("Created Users: admin, teachers, and students");

        // --- Create Class Sections ---
        // Create Mathematics 101 class
        ClassSection mathClass1 = new ClassSection("Mathematics 101", math, fall2024, teacher1);
        mathClass1.addStudent(student1);
        mathClass1.addStudent(student2);
        mathClass1.addStudent(student3);
        classSectionRepository.save(mathClass1);
        
        // Create Physics 101 class
        ClassSection physicsClass = new ClassSection("Physics 101", physics, fall2024, teacher2);
        physicsClass.addStudent(student1);
        physicsClass.addStudent(student2);
        physicsClass.addStudent(student4);
        classSectionRepository.save(physicsClass);
        
        // Create Chemistry 101 class
        ClassSection chemistryClass = new ClassSection("Chemistry 101", chemistry, fall2024, teacher3);
        chemistryClass.addStudent(student1);
        chemistryClass.addStudent(student3);
        chemistryClass.addStudent(student5);
        classSectionRepository.save(chemistryClass);
        
        // Create Biology 101 class
        ClassSection biologyClass = new ClassSection("Biology 101", biology, spring2025, teacher3);
        biologyClass.addStudent(student2);
        biologyClass.addStudent(student4);
        biologyClass.addStudent(student5);
        classSectionRepository.save(biologyClass);
        
        // Create History 101 class
        ClassSection historyClass = new ClassSection("History 101", history, spring2025, teacher1);
        historyClass.addStudent(student1);
        historyClass.addStudent(student3);
        historyClass.addStudent(student5);
        classSectionRepository.save(historyClass);
        
        // Create Mathematics 102 class
        ClassSection mathClass2 = new ClassSection("Mathematics 102", math, spring2025, teacher2);
        mathClass2.addStudent(student2);
        mathClass2.addStudent(student3);
        mathClass2.addStudent(student4);
        classSectionRepository.save(mathClass2);

        logger.info("Created Class Sections with assigned teachers and students");

        // --- Create Grades ---
        // Fall 2024 Grades
        // Student 1 Grades
        gradeRepository.save(new Grade(85.5, "Good effort", student1, math, fall2024));
        gradeRepository.save(new Grade(92.0, "Excellent understanding of concepts", student1, physics, fall2024));
        gradeRepository.save(new Grade(78.0, "Needs more practice with formulas", student1, chemistry, fall2024));

        // Student 2 Grades
        gradeRepository.save(new Grade(91.0, "Excellent work!", student2, math, fall2024));
        gradeRepository.save(new Grade(88.5, "Good lab work", student2, physics, fall2024));

        // Student 3 Grades
        gradeRepository.save(new Grade(75.0, "Improving, but needs more focus", student3, math, fall2024));
        gradeRepository.save(new Grade(81.0, "Good understanding of basic concepts", student3, chemistry, fall2024));

        // Student 4 Grades
        gradeRepository.save(new Grade(94.5, "Outstanding performance", student4, physics, fall2024));

        // Student 5 Grades
        gradeRepository.save(new Grade(87.0, "Good lab reports", student5, chemistry, fall2024));

        // Spring 2025 Grades
        // Student 1 Grades
        gradeRepository.save(new Grade(88.0, "Continued improvement", student1, history, spring2025));

        // Student 2 Grades
        gradeRepository.save(new Grade(90.0, "Excellent research paper", student2, biology, spring2025));
        gradeRepository.save(new Grade(93.5, "Outstanding progress", student2, math, spring2025));

        // Student 3 Grades
        gradeRepository.save(new Grade(82.5, "Good class participation", student3, history, spring2025));
        gradeRepository.save(new Grade(85.0, "Improved problem-solving", student3, math, spring2025));

        // Student 4 Grades
        gradeRepository.save(new Grade(89.0, "Very good lab work", student4, biology, spring2025));
        gradeRepository.save(new Grade(91.5, "Excellent test scores", student4, math, spring2025));

        // Student 5 Grades
        gradeRepository.save(new Grade(84.0, "Good essay writing", student5, history, spring2025));
        gradeRepository.save(new Grade(86.5, "Thorough lab reports", student5, biology, spring2025));

        logger.info("Created initial grades for students across semesters.");
        logger.info("Initial data loading complete.");
    }
}