package com.example.attendance.config;

import com.example.attendance.entity.Admin;
import com.example.attendance.entity.Course;
import com.example.attendance.service.AdminService;
import com.example.attendance.repository.AdminRepository;
import com.example.attendance.repository.CourseRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class DataLoader {

    private final AdminService adminService;
    private final AdminRepository adminRepository;
    private final CourseRepository courseRepository;

    public DataLoader(AdminService adminService, AdminRepository adminRepository, CourseRepository courseRepository) {
        this.adminService = adminService;
        this.adminRepository = adminRepository;
        this.courseRepository = courseRepository;
    }

    @PostConstruct
    public void loadData() {
        System.out.println("========== DATALOADER: STARTUP CHECKS ==========");
        try {
            // Log all admins
            List<Admin> admins = adminRepository.findAll();
            System.out.println("Found " + admins.size() + " admins in database:");
            for (Admin a : admins) {
                System.out.println(" - ID: " + a.getId() + ", Username: " + a.getUsername() + ", Password: " + a.getPassword());
            }

            // Reset or Create the 'admin' user to always have password 'admin123'
            Admin admin = adminRepository.findByUsername("admin").orElse(null);
            if (admin == null) {
                admin = Admin.builder()
                        .username("admin")
                        .password("admin123")
                        .build();
                adminRepository.save(admin);
                System.out.println("Created default admin user: username=admin, password=admin123");
            } else {
                admin.setPassword("admin123");
                adminRepository.save(admin);
                System.out.println("Updated/Reset admin user password to: admin123");
            }

            // Seed default courses if empty
            if (courseRepository.count() == 0) {
                List<Course> defaultCourses = List.of(
                    Course.builder().courseName("Higher National Diploma in Information Technology").courseCode("HNDIT").build(),
                    Course.builder().courseName("Higher National Diploma in Accountancy").courseCode("HNDA").build(),
                    Course.builder().courseName("Higher National Diploma in Business Finance").courseCode("HNDBF").build(),
                    Course.builder().courseName("Higher National Diploma in Business Administration").courseCode("HNDBA").build(),
                    Course.builder().courseName("Higher National Diploma in English").courseCode("HNDENGLISH").build(),
                    Course.builder().courseName("Higher National Diploma in Management").courseCode("HNDM").build(),
                    Course.builder().courseName("Higher National Diploma in Tourism And Hospitality Management").courseCode("HNDTHM").build()
                );
                courseRepository.saveAll(defaultCourses);
                System.out.println("Seeded 7 default courses successfully.");
            } else {
                System.out.println("Courses already seeded in the database. Total courses: " + courseRepository.count());
            }

        } catch (Exception e) {
            System.err.println("Error running DataLoader: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("=================================================");
    }
}