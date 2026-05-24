package com.example.attendance.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // course_id

    @Column(name = "course_name")
    private String courseName;

    @Column(name = "course_code")
    private String courseCode;
}