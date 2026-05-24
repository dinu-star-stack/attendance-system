package com.example.attendance.service;

import com.example.attendance.entity.Admin;
import com.example.attendance.repository.AdminRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public Admin login(String username, String password) {
        return adminRepository.findByUsername(username)
                .filter(admin -> admin.getPassword().equals(password))
                .orElse(null);
    }

    public void save(Admin admin) {
        adminRepository.save(admin);
    }

    public boolean exists(String username) {
        return adminRepository.findByUsername(username).isPresent();
    }
}