package vn.edu.hcmus.mentor.controller.payload.request.note;

import vn.edu.hcmus.mentor.controller.usecase.common.pagination.PageQueryFilter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetNotesByUserRequest extends PageQueryFilter {

    private String search;
}