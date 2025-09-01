package com.richardmogou.StageRichy.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

/**
 * This class contains tag definitions for Swagger documentation.
 * These tags are used to group API endpoints in the Swagger UI.
 */
@Configuration
public class SwaggerConfig {

    @Tag(name = "Authentication", description = "Authentication operations")
    public interface AuthenticationTag {}

    @Tag(name = "Admin", description = "Operations available to administrators only")
    public interface AdminTag {}

    @Tag(name = "Teacher", description = "Operations available to teachers")
    public interface TeacherTag {}

    @Tag(name = "Student", description = "Operations available to students")
    public interface StudentTag {}

    @Tag(name = "Reports", description = "PDF report generation operations")
    public interface ReportsTag {}

    @Tag(name = "Excel Export", description = "Excel export operations")
    public interface ExcelExportTag {}

    @Tag(name = "Statistics", description = "Grade statistics and analytics operations")
    public interface StatisticsTag {}

    @Tag(name = "Class Management", description = "Class and section management operations")
    public interface ClassManagementTag {}

    @Tag(name = "Grade Management", description = "Grade management operations")
    public interface GradeManagementTag {}
}
