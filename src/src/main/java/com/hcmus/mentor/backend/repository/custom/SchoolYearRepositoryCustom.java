package com.hcmus.mentor.backend.repository.custom;

import com.hcmus.mentor.backend.controller.usecase.schoolyear.search.SearchSchoolYearQuery;
import com.hcmus.mentor.backend.domain.SchoolYear;
import org.springframework.data.domain.Page;

public interface SchoolYearRepositoryCustom {

    Page<SchoolYear> search(SearchSchoolYearQuery query);
}
