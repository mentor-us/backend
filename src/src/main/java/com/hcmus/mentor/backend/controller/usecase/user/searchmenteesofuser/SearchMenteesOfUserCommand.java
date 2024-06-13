package com.hcmus.mentor.backend.controller.usecase.user.searchmenteesofuser;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.common.pagination.PageQueryFilter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchMenteesOfUserCommand extends PageQueryFilter implements Command<SearchMenteesOfUserResult> {

    private String query;
}