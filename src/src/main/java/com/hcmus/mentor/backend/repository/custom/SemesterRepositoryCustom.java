package com.hcmus.mentor.backend.repository.custom;

import com.hcmus.mentor.backend.controller.usecase.semester.search.SearchSemesterQuery;
import com.hcmus.mentor.backend.domain.Semester;
import org.springframework.data.domain.Page;

public interface SemesterRepositoryCustom {

    Page<Semester> search(SearchSemesterQuery query);
}
