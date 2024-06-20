package com.hcmus.mentor.backend.controller.usecase.note.deletenote;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.repository.NoteRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DeleteNoteCommandHandler implements Command.Handler<DeleteNoteCommand, Void> {

    private final Logger logger = LoggerFactory.getLogger(DeleteNoteCommandHandler.class);
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    @Transactional
    public Void handle(DeleteNoteCommand command) {
        var note = noteRepository.findById(command.getNoteId()).orElseThrow(() -> new DomainException("Không tim thấy ghi chú"));
        var user = userRepository.findById(loggedUserAccessor.getCurrentUserId()).orElseThrow(() -> new DomainException("Không tìm thấy người cập nhật"));
        if (note.getOwner().getId().equals(user.getId()) || note.getCreator().getId().equals(user.getId())) {
            throw new DomainException("Chỉ owner hoặc người tạo được phép xoá ghi chú");
        }

        noteRepository.delete(note);
        return null;
    }
}