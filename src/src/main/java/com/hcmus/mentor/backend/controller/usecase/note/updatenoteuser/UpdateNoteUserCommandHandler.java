package com.hcmus.mentor.backend.controller.usecase.note.updatenoteuser;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteDetailDto;
import com.hcmus.mentor.backend.controller.usecase.note.updatenote.UpdateNoteCommandHandler;
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
public class UpdateNoteUserCommandHandler implements Command.Handler<UpdateNoteUserCommand, NoteDetailDto> {

    private final Logger logger = LoggerFactory.getLogger(UpdateNoteCommandHandler.class);
    private final ModelMapper modelMapper;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    @Transactional
    public NoteDetailDto handle(UpdateNoteUserCommand command) {
        var note = noteRepository.findById(command.getNoteId()).orElseThrow(() -> new DomainException("Không tim thấy ghi chú"));
        var user = userRepository.findById(loggedUserAccessor.getCurrentUserId()).orElseThrow(() -> new DomainException("Không tìm thấy người cập nhật"));
        if (noteRepository.canEdit(command.getNoteId(), user.getId())) {
            throw new DomainException("Không có quyền chỉnh sửa ghi chú");
        }

        note.setUsers(new HashSet<>(userRepository.findAllById(command.getUserIds())));
        note.setUpdatedDate(DateUtils.getDateNowAtUTC());

        noteRepository.save(note);

        return modelMapper.map(note, NoteDetailDto.class);
    }
}