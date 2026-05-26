package com.example.attendance.controller;

import com.example.attendance.entity.Admin;
import com.example.attendance.entity.Course;
import com.example.attendance.entity.Student;
import com.example.attendance.entity.Lecturer;
import com.example.attendance.service.AdminService;
import com.example.attendance.repository.CourseRepository;
import com.example.attendance.repository.StudentRepository;
import com.example.attendance.repository.LecturerRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;

    public AdminController(AdminService adminService,
                           CourseRepository courseRepository,
                           StudentRepository studentRepository,
                           LecturerRepository lecturerRepository) {
        this.adminService = adminService;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.lecturerRepository = lecturerRepository;
    }

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ===== ADMIN LOGIN =====
    @GetMapping("/login")
    public String loginPage() {
        return "admin-login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          Model model,
                          HttpSession session) {

        Admin admin = adminService.login(username, password);
        if (admin == null) {
            model.addAttribute("error", "Invalid username or password");
            return "admin-login";
        }

        session.setAttribute("admin", admin);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // ===== ADMIN DASHBOARD =====
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }

        List<Course> courses = courseRepository.findAll();

        Map<Long, String> colors = new HashMap<>();
        Random random = new Random();
        for (Course c : courses) {
            int r = random.nextInt(100) + 150;
            int g = random.nextInt(100) + 150;
            int b = random.nextInt(100) + 150;
            colors.put(c.getId(), String.format("rgb(%d,%d,%d)", r, g, b));
        }

        model.addAttribute("courses", courses);
        model.addAttribute("colors", colors);
        return "admin-dashboard";
    }

    // ===== ADD / VIEW STUDENTS =====
    @GetMapping("/course/{id}/add-student")
    public String addStudentForm(@PathVariable Long id, Model model, HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";

        Optional<Course> courseOpt = courseRepository.findById(id);
        if (courseOpt.isEmpty()) return "redirect:/admin/dashboard";

        model.addAttribute("course", courseOpt.get());
        model.addAttribute("student", new Student());
        return "add-student";
    }

    @PostMapping("/course/{id}/save-student")
    public String saveStudent(@PathVariable Long id,
                              @ModelAttribute Student student,
                              Model model,
                              HttpSession session) {

        if (session.getAttribute("admin") == null) return "redirect:/admin/login";

        Optional<Course> courseOpt = courseRepository.findById(id);
        if (courseOpt.isEmpty()) return "redirect:/admin/dashboard";


        // ✅ Check duplicate (registration number)
        Optional<Student> existing = studentRepository.findByRegistrationNumber(student.getRegistrationNumber());

        if (existing.isPresent()) {
            model.addAttribute("course", courseOpt.get());
            model.addAttribute("student", student);
            model.addAttribute("error", "Student already added!");
            return "add-student";
        }

        student.setId(null);
        student.setCourse(courseOpt.get());
        if (student.getPassword() != null && !student.getPassword().isEmpty()) {
            student.setPassword(passwordEncoder.encode(student.getPassword()));
        } else {
            student.setPassword(null);
        }
        studentRepository.save(student);

        return "redirect:/admin/course/" + id + "/add-student?success";
    }

    @GetMapping("/course/{id}/view-students")
    public String viewStudents(@PathVariable Long id, Model model, HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";

        Optional<Course> courseOpt = courseRepository.findById(id);
        if (courseOpt.isEmpty()) return "redirect:/admin/dashboard";

        List<Student> students = studentRepository.findByCourseId(id);

        Map<Integer, List<Student>> studentsByBatch =
                students.stream().collect(Collectors.groupingBy(Student::getBatchYear));

        Map<Integer, List<Student>> sortedBatches =
                new TreeMap<>(Collections.reverseOrder());
        sortedBatches.putAll(studentsByBatch);

        model.addAttribute("course", courseOpt.get());
        model.addAttribute("studentsByBatch", sortedBatches);

        return "view-students";
    }

    // ===== ADD / VIEW LECTURERS =====
    @GetMapping("/course/{id}/add-lecturer")
    public String addLecturerForm(@PathVariable Long id, Model model, HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";

        Optional<Course> courseOpt = courseRepository.findById(id);
        if (courseOpt.isEmpty()) return "redirect:/admin/dashboard";

        model.addAttribute("course", courseOpt.get());
        model.addAttribute("lecturer", new Lecturer());
        return "add-lecturer";
    }

    @PostMapping("/course/{id}/save-lecturer")
    public String saveLecturer(@PathVariable Long id,
                               @ModelAttribute Lecturer lecturer,
                               Model model,
                               HttpSession session) {

        if (session.getAttribute("admin") == null) return "redirect:/admin/login";

        Optional<Course> courseOpt = courseRepository.findById(id);
        if (courseOpt.isEmpty()) return "redirect:/admin/dashboard";

        // ✅ Check duplicate
        Optional<Lecturer> existing = lecturerRepository.findByLecturerId(lecturer.getLecturerId());

        if (existing.isPresent()) {
            model.addAttribute("course", courseOpt.get());
            model.addAttribute("lecturer", lecturer);
            model.addAttribute("error", "Lecturer already added!");
            return "add-lecturer";
        }

        lecturer.setId(null);
        lecturer.setCourses(List.of(courseOpt.get()));
        if (lecturer.getPassword() != null && !lecturer.getPassword().isEmpty()) {
            lecturer.setPassword(passwordEncoder.encode(lecturer.getPassword()));
        } else {
            lecturer.setPassword(null);
        }
        lecturerRepository.save(lecturer);

        return "redirect:/admin/course/" + id + "/add-lecturer?success";
    }

    @GetMapping("/course/{id}/view-lecturers")
    public String viewLecturers(@PathVariable Long id, Model model, HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";

        Optional<Course> courseOpt = courseRepository.findById(id);
        if (courseOpt.isEmpty()) return "redirect:/admin/dashboard";

        List<Lecturer> lecturers = lecturerRepository.findAll().stream()
                .filter(l -> l.getCourses().stream().anyMatch(c -> c.getId().equals(id)))
                .toList();

        model.addAttribute("course", courseOpt.get());
        model.addAttribute("lecturers", lecturers);
        return "view-lecturers";
    }

     @GetMapping("/student/edit/{id}")
    public String editStudent(@PathVariable Long id, Model model, HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";

        Student student = studentRepository.findById(id).orElse(null);
        if (student == null) return "redirect:/admin/dashboard";

        model.addAttribute("student", student);
        return "edit-student";
    }
    @PostMapping("/student/update")
    public String updateStudent(@ModelAttribute Student student, HttpSession session) {

        if (session.getAttribute("admin") == null) return "redirect:/admin/login";

        Student existing = studentRepository.findById(student.getId()).orElse(null);

        if (existing == null) return "redirect:/admin/dashboard";

        // ✅ update fields safely
        existing.setName(student.getName());
        existing.setEmail(student.getEmail());
        existing.setRegistrationNumber(student.getRegistrationNumber());
        existing.setCourseType(student.getCourseType());
        existing.setBatchYear(student.getBatchYear());

        studentRepository.save(existing);

        return "redirect:/admin/course/" + existing.getCourse().getId() + "/view-students";
    }
 @GetMapping("/student/delete/{id}")
    public String deleteStudent(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";

        Student student = studentRepository.findById(id).orElse(null);
        if (student != null) {
            Long courseId = student.getCourse().getId();
            studentRepository.deleteById(id);
            return "redirect:/admin/course/" + courseId + "/view-students";
        }

        return "redirect:/admin/dashboard";
    }
  @GetMapping("/lecturer/edit/{id}")
    public String editLecturer(@PathVariable Long id, Model model, HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";

        Lecturer lecturer = lecturerRepository.findById(id).orElse(null);
        if (lecturer == null) return "redirect:/admin/dashboard";

        model.addAttribute("lecturer", lecturer);
        return "edit-lecturer";
    }
    @PostMapping("/lecturer/update")
    public String updateLecturer(@ModelAttribute Lecturer lecturer, HttpSession session) {

        if (session.getAttribute("admin") == null) return "redirect:/admin/login";

        Lecturer existing = lecturerRepository.findById(lecturer.getId()).orElse(null);
        if (existing == null) return "redirect:/admin/dashboard";

        existing.setName(lecturer.getName());
        existing.setEmail(lecturer.getEmail());
        existing.setLecturerId(lecturer.getLecturerId());

        lecturerRepository.save(existing);

        // ✅ get course id and redirect correctly
        Long courseId = existing.getCourses().get(0).getId();

        return "redirect:/admin/course/" + courseId + "/view-lecturers";
    }
 @GetMapping("/lecturer/delete/{id}")
    public String deleteLecturer(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";

        lecturerRepository.deleteById(id);

        return "redirect:/admin/dashboard";
    }

    @GetMapping("/student/reset-password/{id}")
    public String showResetPasswordPage(@PathVariable Long id, Model model, HttpSession session) {

        if (session.getAttribute("admin") == null) return "redirect:/admin/login";

        Student student = studentRepository.findById(id).orElse(null);
        if (student == null) return "redirect:/admin/dashboard";

        model.addAttribute("student", student);
        return "reset-student-password"; // new page
    }
    @PostMapping("/student/reset-password")
    public String resetStudentPassword(@RequestParam Long id,
                                       @RequestParam String password,
                                       HttpSession session) {

        if (session.getAttribute("admin") == null) return "redirect:/admin/login";

        Student student = studentRepository.findById(id).orElse(null);
        if (student == null) return "redirect:/admin/dashboard";

        student.setPassword(passwordEncoder.encode(password));
        studentRepository.save(student);

        return "redirect:/admin/course/" + student.getCourse().getId() + "/view-students";
    }

    @GetMapping("/lecturer/reset-password/{id}")
    public String showResetLecturerPasswordPage(@PathVariable Long id, Model model, HttpSession session) {

        if (session.getAttribute("admin") == null) return "redirect:/admin/login";

        Lecturer lecturer = lecturerRepository.findById(id).orElse(null);
        if (lecturer == null) return "redirect:/admin/dashboard";

        model.addAttribute("lecturer", lecturer);
        return "reset-lecturer-password";
    }
@PostMapping("/lecturer/reset-password")
    public String resetLecturerPassword(@RequestParam Long id,
                                        @RequestParam String password,
                                        HttpSession session) {

        if (session.getAttribute("admin") == null) return "redirect:/admin/login";

        Lecturer lecturer = lecturerRepository.findById(id).orElse(null);
        if (lecturer == null) return "redirect:/admin/dashboard";

        lecturer.setPassword(passwordEncoder.encode(password));
        lecturerRepository.save(lecturer);

        // redirect back to correct course page
        Long courseId = lecturer.getCourses().get(0).getId();

        return "redirect:/admin/course/" + courseId + "/view-lecturers";
    }

    // ===== REAL-TIME AJAX STUDENT SEARCH API =====
    @GetMapping("/api/search-students")
    @ResponseBody
    public List<Map<String, String>> searchStudents(@RequestParam String name, HttpSession session) {
        if (session.getAttribute("admin") == null) {
            return Collections.emptyList();
        }

        List<Student> students = studentRepository.findAll();
        return students.stream()
                .filter(s -> s.getName().toLowerCase().contains(name.toLowerCase()))
                .map(s -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("name", s.getName());
                    map.put("email", s.getEmail());
                    map.put("regNumber", s.getRegistrationNumber());
                    map.put("batchYear", String.valueOf(s.getBatchYear()));
                    map.put("courseType", s.getCourseType());
                    map.put("courseName", s.getCourse().getCourseName());
                    return map;
                })
                .collect(Collectors.toList());
    }

    // ===== CSV TEMPLATES DOWNLOAD =====
    @GetMapping("/template/students")
    public void downloadStudentTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=students_template.csv");
        response.getWriter().println("Name,Email,Registration Number,Batch Year,Course Type");
        response.getWriter().println("John Doe,johndoe@gmail.com,REG/2026/001,2025,Fulltime");
        response.getWriter().println("Jane Smith,janesmith@gmail.com,REG/2026/002,2023,Parttime");
    }

    @GetMapping("/template/lecturers")
    public void downloadLecturerTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=lecturers_template.csv");
        response.getWriter().println("Name,Email,Lecturer ID");
        response.getWriter().println("Dr. Robert Downey,robert@gmail.com,L201");
        response.getWriter().println("Prof. Albert Einstein,albert@gmail.com,L202");
    }

    // ===== CSV BULK IMPORTS =====
    @PostMapping("/course/{id}/import-students")
    public String importStudents(@PathVariable Long id,
                                 @RequestParam("file") MultipartFile file,
                                 RedirectAttributes redirectAttributes,
                                 HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";

        Course course = courseRepository.findById(id).orElseThrow();

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a CSV file to upload.");
            return "redirect:/admin/dashboard";
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isHeader = true;
            int importedCount = 0;
            int duplicateCount = 0;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (isHeader) {
                    isHeader = false; // skip header row
                    continue;
                }

                String[] data = line.split(",");
                if (data.length < 5) continue;

                String name = data[0].trim();
                String email = data[1].trim();
                String regNumber = data[2].trim();
                
                int batchYear;
                try {
                    batchYear = Integer.parseInt(data[3].trim());
                } catch (NumberFormatException e) {
                    batchYear = 2025; // default fallback
                }
                
                String courseType = data[4].trim();

                // Check duplicate
                if (studentRepository.findByRegistrationNumber(regNumber).isPresent()) {
                    duplicateCount++;
                    continue;
                }

                Student student = Student.builder()
                        .name(name)
                        .email(email)
                        .registrationNumber(regNumber)
                        .batchYear(batchYear)
                        .courseType(courseType)
                        .course(course)
                        .password(null) // first time login will set password
                        .build();

                studentRepository.save(student);
                importedCount++;
            }

            redirectAttributes.addFlashAttribute("successMessage", "Successfully imported " + importedCount + " students to " + course.getCourseCode() + "! (Duplicates skipped: " + duplicateCount + ")");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error importing CSV: " + e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }

    @PostMapping("/course/{id}/import-lecturers")
    public String importLecturers(@PathVariable Long id,
                                  @RequestParam("file") MultipartFile file,
                                  RedirectAttributes redirectAttributes,
                                  HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";

        Course course = courseRepository.findById(id).orElseThrow();

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a CSV file to upload.");
            return "redirect:/admin/dashboard";
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isHeader = true;
            int importedCount = 0;
            int duplicateCount = 0;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] data = line.split(",");
                if (data.length < 3) continue;

                String name = data[0].trim();
                String email = data[1].trim();
                String lecturerId = data[2].trim();

                // Check duplicate
                if (lecturerRepository.findByLecturerId(lecturerId).isPresent()) {
                    duplicateCount++;
                    continue;
                }

                Lecturer lecturer = Lecturer.builder()
                        .name(name)
                        .email(email)
                        .lecturerId(lecturerId)
                        .courses(List.of(course))
                        .password(null) // set on first-time login
                        .build();

                lecturerRepository.save(lecturer);
                importedCount++;
            }

            redirectAttributes.addFlashAttribute("successMessage", "Successfully imported " + importedCount + " lecturers to " + course.getCourseCode() + "! (Duplicates skipped: " + duplicateCount + ")");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error importing CSV: " + e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }
}