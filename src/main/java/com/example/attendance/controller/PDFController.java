package com.example.attendance.controller;

import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Lecture;
import com.example.attendance.entity.Student;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.LectureRepository;
import com.example.attendance.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Controller
@RequiredArgsConstructor
public class PDFController {

    private final AttendanceRepository attendanceRepository;
    private final LectureRepository lectureRepository;
    private final StudentRepository studentRepository;

    // ===== ALL COURSE ATTENDANCE REPORT =====
    @GetMapping("/attendance/pdf")
    public void generatePdf(@RequestParam Long courseId, HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=course_attendance_report.pdf");

        List<Attendance> attendanceList = attendanceRepository.findByCourseId(courseId);

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Paragraph title = new Paragraph("Course Attendance Report", font);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" ")); // empty line

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.addCell("Lecture Name");
        table.addCell("Lecturer Name");
        table.addCell("Student Name");
        table.addCell("Registration Number");
        table.addCell("Status");
        table.addCell("Date");

        for (Attendance att : attendanceList) {
            table.addCell(att.getLecture().getLectureName());
            table.addCell(att.getLecture().getLecturer().getName());
            table.addCell(att.getStudent().getName());
            table.addCell(att.getStudent().getRegistrationNumber());
            table.addCell(att.getStatus());
            table.addCell(att.getDate());
        }

        document.add(table);
        document.close();
    }

    // ===== DUAL PRESENT/ABSENT INDIVIDUAL SESSION REPORT =====
    @GetMapping("/attendance/pdf/lecture/{lectureId}")
    public void generateLecturePdf(@PathVariable Long lectureId, 
                                   @RequestParam(required = false) String timezone,
                                   HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=session_attendance_" + lectureId + ".pdf");

        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid lecture ID"));
        
        List<Student> allCourseStudents = studentRepository.findByCourseId(lecture.getCourse().getId());
        List<Attendance> presentAttendances = attendanceRepository.findByLectureId(lectureId)
                .stream()
                .filter(att -> "Present".equalsIgnoreCase(att.getStatus()))
                .toList();

        // Gather all student IDs who are registered as present
        Set<Long> presentStudentIds = presentAttendances.stream()
                .map(att -> att.getStudent().getId())
                .collect(Collectors.toSet());

        Document document = new Document(PageSize.A4, 36, 36, 54, 36);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        // Colors
        java.awt.Color primaryColor = new java.awt.Color(15, 23, 42); // slate-900
        java.awt.Color accentGreen = new java.awt.Color(5, 150, 105); // emerald-600
        java.awt.Color accentRed = new java.awt.Color(220, 38, 38); // red-600

        // Font styles
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, primaryColor);
        Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new java.awt.Color(100, 116, 139)); // slate-500
        Font normalBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, primaryColor);
        Font normalRegular = FontFactory.getFont(FontFactory.HELVETICA, 9, primaryColor);
        Font presentFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, accentGreen);
        Font absentFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, accentRed);

        // Header Title
        Paragraph campusTitle = new Paragraph("ATI CAMPUS DEHIWALA - SMART ATTENDANCE PORTAL", subTitleFont);
        campusTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(campusTitle);

        Paragraph mainTitle = new Paragraph("DAILY ATTENDANCE SESSION REPORT", titleFont);
        mainTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(mainTitle);
        document.add(new Paragraph(" ")); // Spacer

        // Session Information Panel
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1.2f, 2.8f});
        
        infoTable.addCell(new Phrase("Lecture Topic:", normalBold));
        infoTable.addCell(new Phrase(lecture.getLectureName(), normalRegular));
        
        infoTable.addCell(new Phrase("Course Name:", normalBold));
        infoTable.addCell(new Phrase(lecture.getCourse().getCourseName() + " (" + lecture.getCourse().getCourseCode() + ")", normalRegular));
        
        infoTable.addCell(new Phrase("Lecturer In-Charge:", normalBold));
        infoTable.addCell(new Phrase(lecture.getLecturer().getName() + " (" + lecture.getLecturer().getLecturerId() + ")", normalRegular));
        
        infoTable.addCell(new Phrase("Session Date:", normalBold));
        infoTable.addCell(new Phrase(lecture.getDate(), normalRegular));
        
        String startTimeFormatted = "N/A";
        String endTimeFormatted = "N/A";
        
        if (lecture.getStartTime() != null) {
            java.time.ZoneId serverZone = java.time.ZoneId.systemDefault();
            java.time.ZoneId clientZone = serverZone;
            if (timezone != null && !timezone.trim().isEmpty()) {
                try {
                    clientZone = java.time.ZoneId.of(timezone.trim());
                } catch (Exception e) {
                    // Fallback to server zone if timezone name is invalid
                }
            }
            
            java.time.ZonedDateTime serverStart = lecture.getStartTime().atZone(serverZone);
            java.time.ZonedDateTime clientStart = serverStart.withZoneSameInstant(clientZone);
            startTimeFormatted = clientStart.format(DateTimeFormatter.ofPattern("hh:mm a"));
            
            if (lecture.getEndTime() != null) {
                java.time.ZonedDateTime serverEnd = lecture.getEndTime().atZone(serverZone);
                java.time.ZonedDateTime clientEnd = serverEnd.withZoneSameInstant(clientZone);
                endTimeFormatted = clientEnd.format(DateTimeFormatter.ofPattern("hh:mm a"));
            }
        }

        infoTable.addCell(new Phrase("Session Timing:", normalBold));
        infoTable.addCell(new Phrase(startTimeFormatted + " - " + endTimeFormatted + " (5 min QR limit)", normalRegular));

        document.add(infoTable);
        document.add(new Paragraph(" ")); // Spacer

        // Statistics Summary
        int enrolledCount = allCourseStudents.size();
        int presentCount = presentAttendances.size();
        int absentCount = enrolledCount - presentCount;
        int attendanceRate = enrolledCount > 0 ? (presentCount * 100 / enrolledCount) : 0;

        PdfPTable statTable = new PdfPTable(4);
        statTable.setWidthPercentage(100);
        
        statTable.addCell(new Phrase("Total Enrolled Students", normalBold));
        statTable.addCell(new Phrase("Total Present", normalBold));
        statTable.addCell(new Phrase("Total Absent", normalBold));
        statTable.addCell(new Phrase("Session Attendance Rate", normalBold));

        statTable.addCell(new Phrase(String.valueOf(enrolledCount), normalRegular));
        statTable.addCell(new Phrase(String.valueOf(presentCount), presentFont));
        statTable.addCell(new Phrase(String.valueOf(absentCount), absentFont));
        statTable.addCell(new Phrase(attendanceRate + "%", normalBold));

        document.add(statTable);
        document.add(new Paragraph(" ")); // Spacer

        // Attendance Roster List Title
        Paragraph rosterTitle = new Paragraph("STUDENT ATTENDANCE ROSTER", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, primaryColor));
        document.add(rosterTitle);
        document.add(new Paragraph(" ")); // Small spacer

        // Student Table
        PdfPTable studentTable = new PdfPTable(4);
        studentTable.setWidthPercentage(100);
        studentTable.setWidths(new float[]{0.8f, 1.8f, 2.4f, 1.0f});

        studentTable.addCell(new Phrase("S.No", normalBold));
        studentTable.addCell(new Phrase("Registration No", normalBold));
        studentTable.addCell(new Phrase("Student Name", normalBold));
        studentTable.addCell(new Phrase("Status", normalBold));

        int index = 1;
        for (Student std : allCourseStudents) {
            studentTable.addCell(new Phrase(String.valueOf(index++), normalRegular));
            studentTable.addCell(new Phrase(std.getRegistrationNumber(), normalRegular));
            studentTable.addCell(new Phrase(std.getName(), normalRegular));
            
            boolean isPresent = presentStudentIds.contains(std.getId());
            if (isPresent) {
                studentTable.addCell(new Phrase("Present", presentFont));
            } else {
                studentTable.addCell(new Phrase("Absent", absentFont));
            }
        }

        document.add(studentTable);
        document.close();
    }
}