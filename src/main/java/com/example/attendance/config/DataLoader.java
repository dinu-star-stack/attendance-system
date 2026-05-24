package com.example.attendance.config;

import com.example.attendance.entity.Admin;
import com.example.attendance.service.AdminService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class DataLoader {

    private final AdminService adminService;

    public DataLoader(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostConstruct
    public void loadAdmin() {
        if (!adminService.exists("admin")) {
            Admin admin = Admin.builder()
                    .username("admin")
                    .password("admin123")
                    .build();
            adminService.save(admin);
        }
    }
}