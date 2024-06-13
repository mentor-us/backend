package com.hcmus.mentor.backend.service.impl;

import com.hcmus.mentor.backend.controller.usecase.schoolyear.search.SearchSchoolYearQuery;
import com.hcmus.mentor.backend.domain.SchoolYear;
import com.hcmus.mentor.backend.repository.SchoolYearRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SchoolYearService {

    private final SchoolYearRepository schoolYearRepository;

    public Page<SchoolYear> search(SearchSchoolYearQuery query) {
        return schoolYearRepository.search(query);
    }

    public Optional<SchoolYear> findById(String id) {
        return schoolYearRepository.findById(id);
    }

    public SchoolYear create(SchoolYear schoolYear) {
        return schoolYearRepository.save(schoolYear);
    }

    public SchoolYear update(SchoolYear schoolYear) {
        return schoolYearRepository.save(schoolYear);
    }

    public void delete(SchoolYear schoolYear) {
        schoolYearRepository.delete(schoolYear);
    }
}
