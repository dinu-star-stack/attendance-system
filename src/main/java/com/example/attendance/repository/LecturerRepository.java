package com.example.attendance.repository;

import com.example.attendance.entity.Lecturer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LecturerRepository extends JpaRepository<Lecturer, Long> {
    Optional<Lecturer> findByLecturerId(String lecturerId);
    Optional<Lecturer> findByEmail(String email);
    Optional<Lecturer> findByResetToken(String token);
}