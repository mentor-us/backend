package com.hcmus.mentor.backend.service.impl;

import com.hcmus.mentor.backend.controller.usecase.grade.getgrade.SearchGradeQuery;
import com.hcmus.mentor.backend.domain.Grade;
import com.hcmus.mentor.backend.repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "gradeCache")
public class GradeService {

    private final GradeRepository gradeRepository;

    @Cacheable(value = "grades", key = "#query.userId")
    public Page<Grade> searchGrade(SearchGradeQuery query) {
        return gradeRepository.searchGrade(query);
    }

    @Cacheable(value = "grade", key = "#id", unless = "#result == null")
    public Optional<Grade> findById(String id) {
        return gradeRepository.findById(id);
    }

    @CacheEvict(cacheNames = "grade", allEntries = true)
    public Grade create(Grade grade) {
        return gradeRepository.save(grade);
    }

    @CacheEvict(cacheNames = "grades", allEntries = true)
    public Grade update(Grade grade) {
        return gradeRepository.save(grade);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "grade", key = "#id"),
            @CacheEvict(cacheNames = "grades", allEntries = true)
    })
    public void delete(Grade grade) {
        gradeRepository.delete(grade);
    }
}
