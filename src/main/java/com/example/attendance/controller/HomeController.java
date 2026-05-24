package com.example.attendance.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


import java.util.*;
@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        // Dynamic courses
        model.addAttribute("courses", new String[]{
                "HNDIT - Information Technology",
                "HNDA - Accountancy",
                "HNDBF - Business Finance",
                "HNDBA - Business Administration",
                "HNDENGLISH - English",
                "HNDM - Management",
                "HNDTHM - Tourism & Hospitality Management"
        });

        model.addAttribute("about", "ATI Attendance Management System provides professional attendance tracking for all courses at ATI Dehiwala.");
        model.addAttribute("vision", "To Become the Centre of Excellence in Technological Education");
        model.addAttribute("mission",  "Creating Excellent Higher National and National Diplomates with Modern Technology for Sustainable Development");

        return "home";
    }

    @GetMapping("/courses")
    public String coursesPage(Model model) {

        List<Map<String, String>> courses = new ArrayList<>();

        // 1️⃣ HNDIT
        Map<String, String> hndit = new HashMap<>();
        hndit.put("title", "Higher National Diploma in Information Technology (HNDIT)");
        hndit.put("image", "/images/hndit.jpg");
        hndit.put("description",
                "The Higher National Diploma in Information Technology (HNDIT) program was developed to produce middle-level IT professionals required for modern industry. It provides both theoretical and practical knowledge in computing, software development, networking, and database systems.");
        hndit.put("duration", "2 ½ Years");
        hndit.put("focus", "Software Engineering, Networking, Database Systems, Web Development");
        hndit.put("extra", "Includes Industrial Training and Final Year Research Project.");
        courses.add(hndit);


        // 2️⃣ HNDA
        Map<String, String> hnda = new HashMap<>();
        hnda.put("title", "Higher National Diploma in Accountancy (HNDA)");
        hnda.put("image", "/images/hnda.jpg");
        hnda.put("description",
                "The Higher National Diploma in Accountancy (HNDA) provides strong professional knowledge in accounting, auditing, taxation, and financial management to prepare students for accounting careers.");
        hnda.put("duration", "2 ½ Years");
        hnda.put("focus", "Financial Accounting, Auditing, Taxation, Business Law");
        hnda.put("extra", "Provides pathway for professional accounting qualifications.");
        courses.add(hnda);


        // 3️⃣ HNDBF
        Map<String, String> hndbf = new HashMap<>();
        hndbf.put("title", "Higher National Diploma in Business Finance (HNDBF)");
        hndbf.put("image", "/images/hndbf.jpg");
        hndbf.put("description",
                "The HNDBF program develops analytical and financial management skills required for careers in banking, corporate finance, and financial institutions.");
        hndbf.put("duration", "2 ½ Years");
        hndbf.put("focus", "Corporate Finance, Investment Analysis, Banking, Risk Management");
        hndbf.put("extra", "Focuses on practical financial case studies and business simulations.");
        courses.add(hndbf);


        // 4️⃣ HNDBA
        Map<String, String> hndba = new HashMap<>();
        hndba.put("title", "Higher National Diploma in Business Administration (HNDBA)");
        hndba.put("image", "/images/hndba.jpg");
        hndba.put("description",
                "The HNDBA program prepares students with managerial and leadership skills required to manage modern organizations effectively.");
        hndba.put("duration", "2 ½ Years");
        hndba.put("focus", "Human Resource Management, Marketing, Business Strategy");
        hndba.put("extra", "Emphasizes practical management training and business planning.");
        courses.add(hndba);


        // 5️⃣ HND English
        Map<String, String> hndenglish = new HashMap<>();
        hndenglish.put("title", "Higher National Diploma in English (HND English)");
        hndenglish.put("image", "/images/hndenglish.jpg");
        hndenglish.put("description",
                "The HND English program enhances advanced English language proficiency and communication skills for professional and academic purposes.");
        hndenglish.put("duration", "2 ½ Years");
        hndenglish.put("focus", "Linguistics, Literature, Communication Skills");
        hndenglish.put("extra", "Provides strong foundation for teaching and communication careers.");
        courses.add(hndenglish);


        // 6️⃣ HNDM
        Map<String, String> hndm = new HashMap<>();
        hndm.put("title", "Higher National Diploma in Management (HNDM)");
        hndm.put("image", "/images/hndm.jpg");
        hndm.put("description",
                "The HNDM program develops leadership, strategic planning, and operational management skills for future managers.");
        hndm.put("duration", "2 ½ Years");
        hndm.put("focus", "Business Strategy, Organizational Behavior, Project Management");
        hndm.put("extra", "Includes business research project and practical exposure.");
        courses.add(hndm);


        // 7️⃣ HNDTHM
        Map<String, String> hndthm = new HashMap<>();
        hndthm.put("title", "Higher National Diploma in Tourism & Hospitality Management (HNDTHM)");
        hndthm.put("image", "/images/hndthm.jpg");
        hndthm.put("description",
                "The HNDTHM program prepares students for careers in tourism, hotel management, and hospitality industries.");
        hndthm.put("duration", "2 ½ Years");
        hndthm.put("focus", "Hotel Operations, Tourism Marketing, Event Management");
        hndthm.put("extra", "Includes practical training in hotels and tourism organizations.");
        courses.add(hndthm);


        model.addAttribute("courses", courses);

        return "courses";
    }
}