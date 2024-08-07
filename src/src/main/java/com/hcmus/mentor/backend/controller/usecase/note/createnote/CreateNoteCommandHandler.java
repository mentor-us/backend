package com.hcmus.mentor.backend.controller.usecase.note.createnote;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteDetailDto;
import com.hcmus.mentor.backend.domain.AuditRecord;
import com.hcmus.mentor.backend.domain.Note;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.ActionType;
import com.hcmus.mentor.backend.domain.constant.DomainType;
import com.hcmus.mentor.backend.repository.NoteRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.AuditRecordService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Handler for
 */
@Component
@RequiredArgsConstructor
public class CreateNoteCommandHandler implements Command.Handler<CreateNoteCommand, NoteDetailDto> {

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final AuditRecordService auditRecordService;

    @Override
    public NoteDetailDto handle(CreateNoteCommand command) {
        var creator = userRepository.findById(loggedUserAccessor.getCurrentUserId())
                .orElseThrow(() -> new DomainException("Người tạo ghi chú không tồn tại"));

        var users = userRepository.findAllById(command.getUserIds());
        if (users.isEmpty()) {
            throw new ForbiddenException("Ghi chú phải liên quan tới ít nhất 1 người");
        }

        var userIsNotMenteeOfCreator = users.stream()
                .map(user -> userRepository.isMentorOfUser(user.getId(), creator.getId()) ? null : user.getName())
                .filter(Objects::nonNull).toList();
        if (!userIsNotMenteeOfCreator.isEmpty()) {
            throw new ForbiddenException(
                    String.format("Người tạo ghi chú không phải là mentor của %s",
                            String.join(",", userIsNotMenteeOfCreator)));
        }

        Note note = noteRepository.save(Note.builder()
                .title(command.getTitle())
                .content(command.getContent())
                .creator(creator)
                .owner(creator)
                .updatedBy(creator)
                .users(new HashSet<>(users))
                .build());
        var userIds = users.stream().map(User::getId).toList();
        auditRecordService.save(AuditRecord.builder()
                .action(ActionType.CREATED)
                .domain(DomainType.NOTE)
                .entityId(note.getId())
                .detail(String.format("Ghi chú mới được tạo cho người dùng: %s", users.stream().map(User::getEmail).collect(Collectors.joining(", "))))
                .user(creator)
                .build());

        return modelMapper.map(note, NoteDetailDto.class);
    }
}