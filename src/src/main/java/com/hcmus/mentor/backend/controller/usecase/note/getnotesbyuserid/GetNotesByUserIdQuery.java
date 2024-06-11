package com.hcmus.mentor.backend.controller.usecase.note.getnotesbyuserid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.common.pagination.PageQueryFilter;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetNotesByUserIdQuery extends PageQueryFilter implements Command<Page<NoteDto>> {

        private String userId;

        private String search;
}