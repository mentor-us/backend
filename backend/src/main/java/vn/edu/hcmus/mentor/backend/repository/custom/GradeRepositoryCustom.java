package vn.edu.hcmus.mentor.backend.repository.custom;

import vn.edu.hcmus.mentor.backend.controller.usecase.grade.getgrade.SearchGradeQuery;
import vn.edu.hcmus.mentor.backend.domain.Grade;
import org.springframework.data.domain.Page;

public interface GradeRepositoryCustom {

    Page<Grade> search(SearchGradeQuery query);
}
