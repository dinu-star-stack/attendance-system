package com.example.attendance.repository;

import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Course findByCourseCode(String courseCode);
}