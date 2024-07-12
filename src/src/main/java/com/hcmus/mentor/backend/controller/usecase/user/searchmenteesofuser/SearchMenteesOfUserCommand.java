package com.hcmus.mentor.backend.controller.usecase.user.searchmenteesofuser;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.common.pagination.PageQueryFilter;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchMenteesOfUserCommand extends PageQueryFilter implements Command<SearchMenteesOfUserResult> {

    private String query;
}