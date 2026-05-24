package com.example.attendance.repository;

import com.example.attendance.entity.Lecture;
import com.example.attendance.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {
    List<Lecture> findByCourseId(Long courseId);
    List<Lecture> findByLecturerId(Long lecturerId);
}