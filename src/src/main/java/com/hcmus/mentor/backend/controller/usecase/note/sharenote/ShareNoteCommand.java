package com.hcmus.mentor.backend.controller.usecase.note.sharenote;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteDetailDto;
import com.hcmus.mentor.backend.controller.payload.request.note.NoteUserShareRequest;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShareNoteCommand implements Command<NoteDetailDto> {

        private String userId;

        private String noteId;

        private NoteUserShareRequest users;
}