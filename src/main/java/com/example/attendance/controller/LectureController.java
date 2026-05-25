package com.example.attendance.controller;

import com.example.attendance.entity.Course;
import com.example.attendance.entity.Lecture;
import com.example.attendance.entity.Lecturer;
import com.example.attendance.repository.CourseRepository;
import com.example.attendance.repository.LectureRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequestMapping("/lecture") // Base path for all lecture session endpoints
public class LectureController {

    private final LectureRepository lectureRepository;
    private final CourseRepository courseRepository;

    public LectureController(LectureRepository lectureRepository,
                             CourseRepository courseRepository) {
        this.lectureRepository = lectureRepository;
        this.courseRepository = courseRepository;
    }

    @GetMapping("/start")
    public String showStartForm(HttpSession session, Model model) {
        Lecturer lecturer = (Lecturer) session.getAttribute("loggedLecturer");
        if (lecturer == null) return "redirect:/lecturer/login";

        model.addAttribute("courses", lecturer.getCourses()); // Courses assigned to this lecturer
        return "start-lecture";
    }

    @PostMapping("/start")
    public String startLecture(@RequestParam String lectureName,
                               @RequestParam Long courseId,
                               HttpSession session,
                               Model model) {

        Lecturer lecturer = (Lecturer) session.getAttribute("loggedLecturer");
        if (lecturer == null) return "redirect:/lecturer/login";

        Course course = courseRepository.findById(courseId).orElseThrow();

        String token = UUID.randomUUID().toString();

        Lecture lecture = Lecture.builder()
                .lectureName(lectureName)
                .course(course)
                .lecturer(lecturer)
                .date(LocalDate.now().toString())
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusMinutes(5))
                .qrToken(token)
                .qrGeneratedTime(LocalDateTime.now())
                .active(true)
                .build();

        lectureRepository.save(lecture);

        model.addAttribute("lectureId", lecture.getId());

        return "qr-display";
    }

    @GetMapping("/qr/{lectureId}")
    @ResponseBody
    public String generateQr(@PathVariable Long lectureId, HttpServletRequest request) {

        Lecture lecture = lectureRepository.findById(lectureId).orElseThrow();

        // ✅ FIXED BUG: check expiry against startTime (or endTime), not qrGeneratedTime
        if (lecture.getStartTime().plusMinutes(5).isBefore(LocalDateTime.now())) {
            lecture.setActive(false);
            lectureRepository.save(lecture);
            return "EXPIRED";
        }

        // ✅ THROTTLED ROTATION: only rotate if token is older than 25 seconds
        LocalDateTime now = LocalDateTime.now();
        if (lecture.getQrToken() == null || lecture.getQrGeneratedTime() == null || 
            lecture.getQrGeneratedTime().plusSeconds(25).isBefore(now)) {
            
            String newToken = UUID.randomUUID().toString();
            lecture.setQrToken(newToken);
            lecture.setQrGeneratedTime(now);
            lectureRepository.save(lecture);
        }

        // ✅ DYNAMIC HOST RESOLVER: Build base URL using host header from request.
        // This automatically handles localhost:8080 locally and the real domain on Railway!
        String scheme = request.getScheme(); // http or https
        String hostHeader = request.getHeader("Host"); // e.g. localhost:8080 or domain.up.railway.app
        String baseUrl;
        
        if (hostHeader != null && !hostHeader.isEmpty()) {
            baseUrl = scheme + "://" + hostHeader;
        } else {
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();
            baseUrl = scheme + "://" + serverName + (serverPort == 80 || serverPort == 443 ? "" : ":" + serverPort);
        }

        return baseUrl + "/student/scan?token=" + lecture.getQrToken();
    }

    @GetMapping("/dashboard")
    public String lectureDashboard(HttpSession session, Model model) {
        Lecturer lecturer = (Lecturer) session.getAttribute("loggedLecturer");
        if (lecturer == null) return "redirect:/lecturer/login";

        model.addAttribute("lectures", lectureRepository.findAll());
        return "lecture-dashboard";
    }
}