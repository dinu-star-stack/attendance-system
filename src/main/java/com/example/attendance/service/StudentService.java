package com.example.attendance.service;

import com.example.attendance.entity.Student;
import com.example.attendance.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public void save(Student student) {
        studentRepository.save(student);
    }

    public List<Student> getStudentsByCourse(Long courseId) {
        return studentRepository.findByCourseId(courseId);
    }
}