package com.example.attendance.controller;

import com.example.attendance.entity.Attendance;
import com.example.attendance.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Controller
@RequiredArgsConstructor
public class PDFController {

    private final AttendanceRepository attendanceRepository;

    @GetMapping("/attendance/pdf")
    public void generatePdf(@RequestParam Long courseId, HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=attendance.pdf");

        List<Attendance> attendanceList = attendanceRepository.findByCourseId(courseId);

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Paragraph title = new Paragraph("Attendance Report", font);
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
}