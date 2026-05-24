package com.example.attendance.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lectures")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // lecture_id

    private String lectureName; // lecture title

    @ManyToOne
    @JoinColumn(name = "lecturer_id")
    private Lecturer lecturer; // link to Lecturer entity

    private String date; // lecture date (string is fine)

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course; // FK to courses

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Column(unique = true)
    private String qrToken;

    private boolean active;
    private LocalDateTime qrGeneratedTime;
}