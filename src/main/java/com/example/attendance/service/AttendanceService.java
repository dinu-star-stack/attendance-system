package com.example.attendance.service;

import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Lecture;
import com.example.attendance.entity.Student;
import com.example.attendance.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }

    public Optional<Attendance> getAttendanceById(Long id) {
        return attendanceRepository.findById(id);
    }

    public Attendance saveAttendance(Attendance attendance) {
        return attendanceRepository.save(attendance);
    }

    public void deleteAttendance(Long id) {
        attendanceRepository.deleteById(id);
    }

    // Check if a student already marked attendance for a lecture
    public Optional<Attendance> getByLectureAndStudent(Lecture lecture, Student student) {
        return attendanceRepository.findByLectureAndStudent(lecture, student);
    }
}