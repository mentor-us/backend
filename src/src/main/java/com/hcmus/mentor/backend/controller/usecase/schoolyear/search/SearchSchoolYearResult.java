package com.hcmus.mentor.backend.controller.usecase.schoolyear.search;

import com.hcmus.mentor.backend.controller.usecase.common.pagination.PageQueryResult;
import com.hcmus.mentor.backend.controller.usecase.schoolyear.common.SchoolYearDto;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SearchSchoolYearResult extends PageQueryResult<SchoolYearDto> {
}