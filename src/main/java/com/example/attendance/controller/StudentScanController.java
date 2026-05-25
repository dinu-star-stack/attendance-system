package com.example.attendance.controller;

import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Lecture;
import com.example.attendance.entity.Student;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.LectureRepository;
import com.example.attendance.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class StudentScanController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Show QR scan page
    @GetMapping("/student/scan")
    public String showScanPage(@RequestParam(required = false) String token, Model model) {
        model.addAttribute("token", token);
        return "student-scan"; // Thymeleaf page
    }

    // Handle QR scan
    @GetMapping("/student/mark-attendance")
    public String markAttendance(@RequestParam String token,
                                 HttpSession session,
                                 Model model) {

        // Get logged-in student
        Student student = (Student) session.getAttribute("loggedStudent");
        if (student == null) {
            model.addAttribute("error", "You must log in first!");
            return "student-login";
        }

        // Find lecture by QR token
        Optional<Lecture> lectureOpt = lectureRepository.findAll()
                .stream()
                .filter(l -> token.equals(l.getQrToken()) && l.isActive())
                .findFirst();

        if (lectureOpt.isEmpty()) {
            model.addAttribute("error", "Invalid or expired QR token.");
            return "student-scan";
        }

        Lecture lecture = lectureOpt.get();

        // Check if attendance already marked
        Optional<Attendance> existing = attendanceRepository.findByLectureAndStudent(lecture, student);
        if (existing.isPresent()) {
            model.addAttribute("message", "Attendance already marked for this lecture.");
            return "student-scan";
        }

        // Mark attendance
        Attendance attendance = Attendance.builder()
                .student(student)
                .lecture(lecture)
                .course(student.getCourse())
                .status("Present")
                .date(lecture.getDate())
                .build();

        attendanceRepository.save(attendance);

        // ✅ Broadcast successful attendance over WebSockets in real-time
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("studentName", student.getName());
            payload.put("registrationNumber", student.getRegistrationNumber());
            payload.put("time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
            
            messagingTemplate.convertAndSend("/topic/attendance/" + lecture.getId(), payload);
        } catch (Exception e) {
            System.err.println("WebSocket Broadcast Error: " + e.getMessage());
        }

        model.addAttribute("message", "Attendance marked successfully!");
        model.addAttribute("lectureName", lecture.getLectureName());
        model.addAttribute("date", lecture.getDate());
        model.addAttribute("courseName", lecture.getCourse().getCourseName());

        return "student-scan-success"; // page shows success
    }
}