package com.hcmus.mentor.backend.controller.usecase.note.updatenote;

import an.awesome.pipelinr.Command;
import com.google.common.base.Strings;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
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

import java.util.HashSet;

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
        var note = noteRepository.findById(command.getNoteId()).orElseThrow(() -> new DomainException("Không tim thấy ghi chú"));

        var updater = userRepository.findById(loggedUserAccessor.getCurrentUserId()).orElseThrow(() -> new DomainException("Không tìm thấy người cập nhật"));

        if (noteRepository.canEdit(command.getNoteId(), updater.getId())) {
            throw new ForbiddenException("Không có quyền chỉnh sửa ghi chú");
        }

        var isUpdate = false;
        var isAddHistory = false;

        var noteHistory = NoteHistory.builder()
                .note(note)
                .updatedBy(updater)
                .title(note.getTitle())
                .content(note.getContent())
                .build();

        if (!Strings.isNullOrEmpty(command.getTitle()) && !command.getTitle().equals(note.getTitle())) {
            note.setTitle(command.getTitle());
            isUpdate = true;
            isAddHistory = true;
        }
        if (!Strings.isNullOrEmpty(command.getContent()) && !command.getContent().equals(note.getContent())) {
            note.setContent(command.getContent());
            isUpdate = true;
            isAddHistory = true;
        }
        if (command.getUserIds() != null && !command.getUserIds().isEmpty()) {
            note.setUsers(new HashSet<>(userRepository.findAllById(command.getUserIds())));
            isUpdate = true;
        }

        if (Boolean.TRUE.equals(isAddHistory)) {
            note.getNoteHistories().add(noteHistory);

            logger.info("Add note history with Id {}, NoteId {}, UpdaterId {}, UpdaterName {}", noteHistory.getId(), note.getId(), updater.getId(), updater.getName());
        }
        if (Boolean.TRUE.equals(isUpdate)) {
            note.setUpdatedBy(updater);
            note.setUpdatedDate(DateUtils.getDateNowAtUTC());
            noteRepository.save(note);

            logger.info("Update note with NoteId {}, UpdaterId {}, UpdaterName {}", note.getId(), updater.getId(), updater.getName());
        }

        return modelMapper.map(note, NoteDetailDto.class);
    }
}