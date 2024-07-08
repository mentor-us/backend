package com.hcmus.mentor.backend.repository.custom;

import com.hcmus.mentor.backend.controller.usecase.grade.getgrade.SearchGradeQuery;
import com.hcmus.mentor.backend.domain.Grade;
import org.springframework.data.domain.Page;

public interface GradeRepositoryCustom {

    Page<Grade> search(SearchGradeQuery query);
}
