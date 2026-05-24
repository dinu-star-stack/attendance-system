package com.example.attendance.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;

    @Column(unique = true)
    private String registrationNumber; // PK reference in attendance

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course; // FK to courses table

    private String courseType; // Fulltime / Parttime
    private String password;
    private int batchYear;
    private String resetToken;
    private java.time.LocalDateTime tokenExpiry;

}