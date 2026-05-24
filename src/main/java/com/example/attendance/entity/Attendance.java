package com.example.attendance.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "attendance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // idd

    private String status; // Present / Absent

    @ManyToOne
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student; // registrationNumber reference

    private String date; // attendance date

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
}