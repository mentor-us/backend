package com.hcmus.mentor.backend.service.impl;

import com.hcmus.mentor.backend.controller.usecase.grade.getgrade.SearchGradeQuery;
import com.hcmus.mentor.backend.domain.Grade;
import com.hcmus.mentor.backend.repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "gradeCache")
public class GradeService {

    private final GradeRepository gradeRepository;

    public Page<Grade> searchGrade(SearchGradeQuery query) {
        return gradeRepository.searchGrade(query);
    }

    public Optional<Grade> findById(String id) {
        return gradeRepository.findById(id);
    }

    public Grade create(Grade grade) {
        return gradeRepository.save(grade);
    }

    public Grade update(Grade grade) {
        return gradeRepository.save(grade);
    }

    public void delete(Grade grade) {
        gradeRepository.delete(grade);
    }
}
