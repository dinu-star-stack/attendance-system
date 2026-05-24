package com.example.attendance.repository;

import com.example.attendance.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByRegistrationNumber(String registrationNumber);

    List<Student> findByCourseId(Long courseId);  // Get all students in a course
    List<Student> findByCourseIdAndBatchYear(Long courseId,Integer batchYear);
    Optional<Student> findByEmail(String email);
    Optional<Student> findByResetToken(String token);
}