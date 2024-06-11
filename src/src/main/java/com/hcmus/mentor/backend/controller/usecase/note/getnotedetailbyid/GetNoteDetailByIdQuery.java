package com.hcmus.mentor.backend.controller.usecase.note.getnotedetailbyid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteDetailDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetNoteDetailByIdQuery implements Command<NoteDetailDto> {

    private String noteId;
}