package com.hcmus.mentor.backend.controller.usecase.note.sharenote;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
import com.hcmus.mentor.backend.controller.payload.request.note.NoteUserShareRequest;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteDetailDto;
import com.hcmus.mentor.backend.domain.NoteUserAccess;
import com.hcmus.mentor.backend.repository.NoteRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ShareNoteCommandHandler implements Command.Handler<ShareNoteCommand, NoteDetailDto> {

    private final Logger logger = LoggerFactory.getLogger(ShareNoteCommandHandler.class);
    private final ModelMapper modelMapper;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    @Transactional
    public NoteDetailDto handle(ShareNoteCommand command) {
        var note = noteRepository.findById(command.getNoteId()).orElseThrow(() -> new DomainException("Không tim thấy ghi chú"));
        var user = userRepository.findById(loggedUserAccessor.getCurrentUserId()).orElseThrow(() -> new DomainException("Không tìm thấy người cập nhật"));
        if (!note.getOwner().getId().equals(user.getId())) {
            throw new ForbiddenException("Chỉ người sở hữu được phép chia sẻ ghi chú");
        }

        var userIds = command.getUsers().stream().map(NoteUserShareRequest::getUserId).toList();
        var users = userRepository.findAllByIdIn(userIds);
        var mapUserAccessRequest = command.getUsers().stream().collect(Collectors.toMap(NoteUserShareRequest::getUserId, NoteUserShareRequest::getAccessType));

        var oldAccesses = note.getUserAccesses();
        oldAccesses.stream().filter(ua -> !userIds.contains(ua.getUser().getId())).toList().forEach(oldAccesses::remove);
        oldAccesses.forEach(userAccess -> userAccess.setNotePermission(mapUserAccessRequest.get(userAccess.getUser().getId())));
        var userAssesses = oldAccesses.stream().map(NoteUserAccess::getUser).toList();
        users.stream().filter(u -> !userAssesses.contains(u)).forEach(u -> {
            oldAccesses.add(NoteUserAccess.builder()
                    .notePermission(mapUserAccessRequest.get(u.getId())).user(u).note(note).build());
        });

        note.setShareType(command.getShareType());
        note.setUserAccesses(oldAccesses);

        noteRepository.save(note);
        return modelMapper.map(note, NoteDetailDto.class);
    }
}