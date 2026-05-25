package com.example.attendance.repository;

import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Lecture;
import com.example.attendance.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByLectureAndStudent(Lecture lecture, Student student);
    List<Attendance> findByCourseId(Long courseId);
    List<Attendance> findByLectureId(Long lectureId);
}