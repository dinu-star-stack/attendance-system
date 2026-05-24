package com.example.attendance.controller;

import com.example.attendance.entity.Lecturer;
import com.example.attendance.repository.LecturerRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.attendance.service.EmailService;
import java.time.LocalDateTime;
import java.util.UUID;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/lecturer")
public class LecturerAuthController {

    private final LecturerRepository lecturerRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Value("${app.reset-password.base-url:http://localhost:8080}")
    private String baseUrl;


    // ====== LOGIN PAGE ======
    @GetMapping("/login")
    public String loginPage() {
        return "lecturer-login"; // Thymeleaf template
    }

    // ====== HANDLE LOGIN ======
    @PostMapping("/login")
    public String login(@RequestParam String lecturerId,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {

        Optional<Lecturer> optionalLecturer = lecturerRepository.findByLecturerId(lecturerId);

        if (optionalLecturer.isEmpty()) {
            model.addAttribute("error", "Lecturer ID not found!");
            return "lecturer-login";
        }

        Lecturer lecturer = optionalLecturer.get();

        // ===== FIRST TIME LOGIN =====
        if (lecturer.getPassword() == null || lecturer.getPassword().isEmpty()) {

            return "redirect:/lecturer/set-password?id="
                    + lecturer.getLecturerId();
        }

        // Check password
        if (!passwordEncoder.matches(password, lecturer.getPassword())) {
            model.addAttribute("error", "Invalid password!");
            return "lecturer-login";
        }

        // SUCCESS: store in session and go to dashboard
        session.setAttribute("loggedLecturer", lecturer);
        return "redirect:/lecturer/dashboard";
    }

    // ===== FIRST TIME LOGIN CHECK =====
    @PostMapping("/first-time-login")
    public String firstTimeLogin(
            @RequestParam String lecturerId,
            Model model) {

        Optional<Lecturer> optionalLecturer =
                lecturerRepository.findByLecturerId(lecturerId);

        if (optionalLecturer.isEmpty()) {

            model.addAttribute("error",
                    "Invalid Lecturer ID!");

            return "lecturer-login";
        }

        Lecturer lecturer = optionalLecturer.get();

        // Already has password
        if (lecturer.getPassword() != null &&
                !lecturer.getPassword().isEmpty()) {

            model.addAttribute("error",
                    "Password already created. Please login normally.");

            return "lecturer-login";
        }

        return "redirect:/lecturer/set-password?id="
                + lecturerId;
    }

    // ===== FIRST TIME SET PASSWORD PAGE =====
    @GetMapping("/set-password")
    public String setPasswordPage(@RequestParam(required = false) String id,
                                  Model model) {

        if (id == null || id.isEmpty()) {
            return "redirect:/lecturer/login";
        }

        model.addAttribute("lecturerId", id);

        return "lecturer-set-password";
    }

    // ===== SAVE FIRST PASSWORD =====
    @PostMapping("/set-password")
    public String savePassword(@RequestParam String lecturerId,
                               @RequestParam String password) {

        Lecturer lecturer = lecturerRepository
                .findByLecturerId(lecturerId)
                .orElseThrow(() -> new RuntimeException("Lecturer not found"));

        lecturer.setPassword(passwordEncoder.encode(password));

        lecturerRepository.save(lecturer);

        return "redirect:/lecturer/login";
    }



    // ====== DASHBOARD ======
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Lecturer lecturer = (Lecturer) session.getAttribute("loggedLecturer");
        if (lecturer == null) {
            return "redirect:/lecturer/login";
        }
        model.addAttribute("lecturer", lecturer);
        return "lecturer-dashboard"; // Thymeleaf template
    }

    // ====== LOGOUT ======
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/lecturer/login";
    }

    // ===== FORGOT PASSWORD PAGE =====
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "lecturer-forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String lecturerId,
                                        @RequestParam String email,
                                        Model model) {

        if (lecturerId == null || email == null) {
            model.addAttribute("error", "Lecturer ID and Email are required!");
            return "lecturer-forgot-password";
        }

        String trimmedLecturerId = lecturerId.trim();
        String trimmedEmail = email.trim();

        Optional<Lecturer> optionalLecturer =
                lecturerRepository.findByLecturerId(trimmedLecturerId);

        if (optionalLecturer.isEmpty()) {

            model.addAttribute("error", "Invalid Lecturer ID!");
            return "lecturer-forgot-password";
        }

        Lecturer lecturer = optionalLecturer.get();

        // Check email matches (case-insensitive & trimmed)
        if (lecturer.getEmail() == null || !lecturer.getEmail().trim().equalsIgnoreCase(trimmedEmail)) {

            model.addAttribute("error",
                    "Email does not match this lecturer!");

            return "lecturer-forgot-password";
        }

        String token = UUID.randomUUID().toString();

        lecturer.setResetToken(token);

        lecturer.setTokenExpiry(
                LocalDateTime.now().plusMinutes(15)
        );

        lecturerRepository.save(lecturer);

        String link = baseUrl + "/lecturer/reset-password?token=" + token;

        emailService.sendResetEmail(
                lecturer.getEmail(),
                link
        );

        model.addAttribute(
                "message",
                "Password reset link sent to your email!"
        );

        return "lecturer-forgot-password";
    }

    // ===== RESET PASSWORD PAGE =====
    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token,
                                    Model model) {

        Optional<Lecturer> optionalLecturer =
                lecturerRepository.findByResetToken(token);

        if (optionalLecturer.isEmpty()) {

            model.addAttribute("error", "Invalid reset token!");

            return "lecturer-forgot-password";
        }

        Lecturer lecturer = optionalLecturer.get();

        // Token expired
        if (lecturer.getTokenExpiry()
                .isBefore(LocalDateTime.now())) {

            model.addAttribute("error", "Reset token expired!");

            return "lecturer-forgot-password";
        }

        model.addAttribute("token", token);

        return "lecturer-reset-password";
    }

    // ===== SAVE NEW PASSWORD =====
    @PostMapping("/reset-password")
    public String saveNewPassword(@RequestParam String token,
                                  @RequestParam String password,
                                  Model model) {

        Lecturer lecturer =
                lecturerRepository.findByResetToken(token)
                        .orElse(null);

        if (lecturer == null) {

            model.addAttribute("error", "Invalid token!");

            return "lecturer-forgot-password";
        }

        // Save new password
        lecturer.setPassword(
                passwordEncoder.encode(password)
        );

        // Clear token
        lecturer.setResetToken(null);
        lecturer.setTokenExpiry(null);

        lecturerRepository.save(lecturer);

        return "redirect:/lecturer/login";
    }
}