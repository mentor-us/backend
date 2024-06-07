package com.hcmus.mentor.backend.controller.usecase.note.createnote;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteDetailDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handler for
 */
@Component
@RequiredArgsConstructor
public class CreateNoteCommandHandler implements Command.Handler<CreateNoteCommand, NoteDetailDto> {
    @Override
    public NoteDetailDto handle(CreateNoteCommand command) {
        return null;
    }
}