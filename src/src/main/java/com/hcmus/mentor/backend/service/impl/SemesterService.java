package com.hcmus.mentor.backend.service.impl;

import com.hcmus.mentor.backend.controller.usecase.semester.search.SearchSemesterQuery;
import com.hcmus.mentor.backend.domain.Semester;
import com.hcmus.mentor.backend.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SemesterService {

    private final SemesterRepository semesterRepository;

    public Page<Semester> search(SearchSemesterQuery query) {
        return semesterRepository.search(query);
    }

    public Optional<Semester> findById(String id) {
        return semesterRepository.findById(id);
    }

    public boolean exists(String name) {
        return semesterRepository.existsByName(name);
    }

    public Semester create(Semester semester) {
        return semesterRepository.save(semester);
    }

    public Semester update(Semester semester) {
        return semesterRepository.save(semester);
    }

    public void delete(Semester semester) {
        semesterRepository.delete(semester);
    }
}
