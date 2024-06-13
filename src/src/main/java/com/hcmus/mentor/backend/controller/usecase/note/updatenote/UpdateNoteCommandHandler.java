package com.hcmus.mentor.backend.controller.usecase.note.updatenote;

import an.awesome.pipelinr.Command;
import com.google.common.base.Strings;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteDetailDto;
import com.hcmus.mentor.backend.domain.NoteHistory;
import com.hcmus.mentor.backend.repository.NoteRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.util.DateUtils;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UpdateNoteCommandHandler implements Command.Handler<UpdateNoteCommand, NoteDetailDto> {

    private final Logger logger = LoggerFactory.getLogger(UpdateNoteCommandHandler.class);
    private final ModelMapper modelMapper;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    @Transactional
    public NoteDetailDto handle(UpdateNoteCommand command) {
        var note = noteRepository.findById(command.getNoteId())
                .orElseThrow(() -> new DomainException("Không tim thấy ghi chú"));
        var updator = userRepository.findById(loggedUserAccessor.getCurrentUserId())
                .orElseThrow(() -> new DomainException("Không tìm thấy người cập nhật"));
        var isUpdate = false;

        var noteHistory = NoteHistory.builder()
                .note(note)
                .updatedBy(updator)
                .title(note.getTitle())
                .content(note.getContent())
                .build();

        if (!Strings.isNullOrEmpty(command.getTitle()) && !command.getTitle().equals(note.getTitle())) {
            note.setTitle(command.getTitle());
            isUpdate = true;
        }

        if (!Strings.isNullOrEmpty(command.getContent()) && !command.getContent().equals(note.getContent())) {
            note.setContent(command.getContent());
            isUpdate = true;
        }

        if (Boolean.TRUE.equals(isUpdate)) {
            note.setUpdatedBy(updator);
            note.setUpdatedDate(DateUtils.getDateNowAtUTC());
            note.getNoteHistories().add(noteHistory);
            noteRepository.save(note);
        }
        return modelMapper.map(note, NoteDetailDto.class);
    }
}