package vn.edu.hcmus.mentor.backend.controller.usecase.note.getnotesbyuserid;

import an.awesome.pipelinr.Command;
import vn.edu.hcmus.mentor.backend.controller.usecase.common.pagination.PageQueryFilter;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetNotesByUserIdQuery extends PageQueryFilter implements Command<GetNoteResult> {

        private String userId;

        private String search;
}