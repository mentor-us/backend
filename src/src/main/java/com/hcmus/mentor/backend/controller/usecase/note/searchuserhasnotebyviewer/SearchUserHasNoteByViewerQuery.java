package com.hcmus.mentor.backend.controller.usecase.note.searchuserhasnotebyviewer;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.common.pagination.PageQueryFilter;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchUserHasNoteByViewerQuery extends PageQueryFilter implements Command<SearchUserHasNoteByViewerResult> {

    private String query;
}