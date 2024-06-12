package com.hcmus.mentor.backend.controller.usecase.semester.search;

import com.hcmus.mentor.backend.controller.usecase.common.pagination.PageQueryResult;
import com.hcmus.mentor.backend.controller.usecase.semester.common.SemesterDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
public class SearchSemesterResult extends PageQueryResult<SemesterDto> {
}
