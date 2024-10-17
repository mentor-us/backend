package vn.edu.hcmus.mentor.repository.custom;

import vn.edu.hcmus.mentor.controller.usecase.grade.getgrade.SearchGradeQuery;
import vn.edu.hcmus.mentor.domain.Grade;
import org.springframework.data.domain.Page;

public interface GradeRepositoryCustom {

    Page<Grade> search(SearchGradeQuery query);
}
