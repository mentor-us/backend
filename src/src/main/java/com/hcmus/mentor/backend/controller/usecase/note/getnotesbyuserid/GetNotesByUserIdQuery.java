package com.hcmus.mentor.backend.controller.usecase.note.getnotesbyuserid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GetNotesByUserIdQuery implements Command<List<NoteDto>> {

        private String viewerId;

        private String userId;

        private String search;

        private Integer page;

        private Integer size;
}