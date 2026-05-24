package com.example.attendance.service;

import com.example.attendance.entity.Lecture;
import com.example.attendance.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LectureService {

    private final LectureRepository lectureRepository;

    public List<Lecture> getAllLectures() {
        return lectureRepository.findAll();
    }

    public Optional<Lecture> getLectureById(Long id) {
        return lectureRepository.findById(id);
    }

    public Lecture saveLecture(Lecture lecture) {
        return lectureRepository.save(lecture);
    }

    public void deleteLecture(Long id) {
        lectureRepository.deleteById(id);
    }

}