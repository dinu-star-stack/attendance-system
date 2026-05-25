package com.example.attendance.controller;

import com.example.attendance.entity.Lecture;
import com.example.attendance.entity.Attendance;
import com.example.attendance.repository.LectureRepository;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.entity.Student;
import com.example.attendance.repository.StudentRepository;
import com.example.attendance.service.EmailService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/student")
public class StudentAuthController {

    private final StudentRepository studentRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final LectureRepository lectureRepository;
    private final AttendanceRepository attendanceRepository;

    @Value("${app.reset-password.base-url:http://localhost:8080}")
    private String baseUrl;



    // ====== LOGIN PAGE ======
    @GetMapping("/login")
    public String loginPage() {
        return "student-login"; // Thymeleaf template
    }

    // ====== HANDLE LOGIN ======
    @PostMapping("/login")
    public String login(@RequestParam String registrationNumber,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {

        Optional<Student> optionalStudent = studentRepository.findByRegistrationNumber(registrationNumber);

        if (optionalStudent.isEmpty()) {
            model.addAttribute("error", "Registration number not found!");
            return "student-login";
        }

        Student student = optionalStudent.get();

        // FIRST TIME LOGIN
        if (student.getPassword() == null || student.getPassword().isEmpty()) {

            return "redirect:/student/set-password?reg=" + registrationNumber;
        }

        // Check password
        if (!passwordEncoder.matches(password, student.getPassword())) {
            model.addAttribute("error", "Invalid password!");
            return "student-login";
        }

        // SUCCESS: store in session and go to dashboard
        session.setAttribute("loggedStudent", student);
        return "redirect:/student/dashboard";
    }

    // ===== FIRST TIME LOGIN CHECK =====
    @PostMapping("/first-time-login")
    public String firstTimeLogin(
            @RequestParam String registrationNumber,
            Model model) {

        Optional<Student> optionalStudent =
                studentRepository.findByRegistrationNumber(registrationNumber);

        if (optionalStudent.isEmpty()) {

            model.addAttribute("error",
                    "Invalid Registration Number!");

            return "student-login";
        }

        Student student = optionalStudent.get();

        // Already has password
        if (student.getPassword() != null &&
                !student.getPassword().isEmpty()) {

            model.addAttribute("error",
                    "Password already created. Please login normally.");

            return "student-login";
        }

        return "redirect:/student/set-password?reg="
                + registrationNumber;
    }

    // ===== FIRST TIME SET PASSWORD PAGE =====
    @GetMapping("/set-password")
    public String setPasswordPage(
            @RequestParam(required = false) String reg,
            Model model) {

        if (reg == null || reg.isEmpty()) {
            return "redirect:/student/login";
        }

        model.addAttribute("registrationNumber", reg);

        return "student-set-password";
    }

    // ===== SAVE FIRST PASSWORD =====
    @PostMapping("/set-password")
    public String savePassword(@RequestParam String registrationNumber,
                               @RequestParam String password) {

        Student student = studentRepository
                .findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        student.setPassword(passwordEncoder.encode(password));

        studentRepository.save(student);

        return "redirect:/student/login";
    }

    // ====== DASHBOARD ======
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Student student = (Student) session.getAttribute("loggedStudent");
        if (student == null) {
            return "redirect:/student/login";
        }
        
        // Compute attendance progress stats
        List<Lecture> totalLectures = lectureRepository.findByCourseId(student.getCourse().getId());
        List<Attendance> presentAttendances = attendanceRepository.findByCourseId(student.getCourse().getId())
                .stream()
                .filter(att -> att.getStudent().getId().equals(student.getId()) && "Present".equalsIgnoreCase(att.getStatus()))
                .toList();

        int totalConducted = totalLectures.size();
        int totalAttended = presentAttendances.size();
        int attendancePercentage = totalConducted > 0 ? (totalAttended * 100 / totalConducted) : 100;

        model.addAttribute("student", student);
        model.addAttribute("totalConducted", totalConducted);
        model.addAttribute("totalAttended", totalAttended);
        model.addAttribute("attendancePercentage", attendancePercentage);
        
        return "student-dashboard"; // Thymeleaf template
    }

    // ====== LOGOUT ======
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/student/login";
    }

    @Autowired
    private EmailService emailService;

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "student-forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String registrationNumber,
                                        @RequestParam String email,
                                        Model model) {

        if (registrationNumber == null || email == null) {
            model.addAttribute("error", "Registration Number and Email are required!");
            return "student-forgot-password";
        }

        String trimmedReg = registrationNumber.trim();
        String trimmedEmail = email.trim();

        Optional<Student> optionalStudent =
                studentRepository.findByRegistrationNumber(trimmedReg);

        if (optionalStudent.isEmpty()) {

            model.addAttribute("error", "Invalid Registration Number!");
            return "student-forgot-password";
        }

        Student student = optionalStudent.get();

        // Check email matches (case-insensitive & trimmed)
        if (student.getEmail() == null || !student.getEmail().trim().equalsIgnoreCase(trimmedEmail)) {

            model.addAttribute("error", "Email does not match this student!");
            return "student-forgot-password";
        }

        String token = UUID.randomUUID().toString();

        student.setResetToken(token);
        student.setTokenExpiry(LocalDateTime.now().plusMinutes(15));

        studentRepository.save(student);

        String link = baseUrl + "/student/reset-password?token=" + token;

        emailService.sendResetEmail(student.getEmail(), link);

        model.addAttribute("message",
                "Password reset link sent to your email!");

        return "student-forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {

        Optional<Student> optionalStudent = studentRepository.findByResetToken(token);

        if (optionalStudent.isEmpty()) {
            model.addAttribute("error", "Invalid token");
            return "student-forgot-password";
        }

        Student student = optionalStudent.get();

        if (student.getTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
            model.addAttribute("error", "Token expired");
            return "student-forgot-password";
        }

        model.addAttribute("token", token);
        return "student-reset-password";
    }
    @PostMapping("/reset-password")
    public String saveNewPassword(@RequestParam String token,
                                  @RequestParam String password,
                                  Model model) {

        Student student = studentRepository.findByResetToken(token).orElse(null);

        if (student == null) {
            model.addAttribute("error", "Invalid token");
            return "student-forgot-password";
        }

        student.setPassword(passwordEncoder.encode(password));
        student.setResetToken(null);
        student.setTokenExpiry(null);

        studentRepository.save(student);

        return "redirect:/student/login";
    }
}