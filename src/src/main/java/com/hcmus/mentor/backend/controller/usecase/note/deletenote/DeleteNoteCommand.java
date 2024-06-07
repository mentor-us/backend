package com.hcmus.mentor.backend.controller.usecase.note.deletenote;

import an.awesome.pipelinr.Command;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeleteNoteCommand implements Command<Void> {

    private String userId;

    private String noteId;
}