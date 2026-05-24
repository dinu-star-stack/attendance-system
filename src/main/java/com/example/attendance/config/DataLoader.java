package com.example.attendance.config;

import com.example.attendance.entity.Admin;
import com.example.attendance.service.AdminService;
import com.example.attendance.repository.AdminRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class DataLoader {

    private final AdminService adminService;
    private final AdminRepository adminRepository;

    public DataLoader(AdminService adminService, AdminRepository adminRepository) {
        this.adminService = adminService;
        this.adminRepository = adminRepository;
    }

    @PostConstruct
    public void loadAdmin() {
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
        } catch (Exception e) {
            System.err.println("Error running DataLoader: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("=================================================");
    }
}