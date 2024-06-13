package com.hcmus.mentor.backend.controller.payload.request.note;

import com.hcmus.mentor.backend.controller.usecase.common.pagination.PageQueryFilter;
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