package com.example.attendance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "lecturers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lecturer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String lecturerId;   // login username

    private String email;

    private String password;

    // Lecturer can teach multiple courses
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "lecturer_courses",
            joinColumns = @JoinColumn(name = "lecturer_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private List<Course> courses;

    private String resetToken;
    private java.time.LocalDateTime tokenExpiry;
}