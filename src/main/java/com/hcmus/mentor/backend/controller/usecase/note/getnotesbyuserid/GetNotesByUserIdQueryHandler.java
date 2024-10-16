package com.hcmus.mentor.backend.controller.usecase.note.getnotesbyuserid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteDto;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteEditableProjection;
import com.hcmus.mentor.backend.repository.NoteRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.util.MappingUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetNotesByUserIdQueryHandler implements Command.Handler<GetNotesByUserIdQuery, GetNoteResult> {

    private final NoteRepository noteRepository;
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final UserRepository userRepository;

    @Override
    public GetNoteResult handle(GetNotesByUserIdQuery command) {

        userRepository.findById(command.getUserId()).orElseThrow(() -> new DomainException("Không tìm thấy người dùng"));

        var pageable = PageRequest.of(command.getPage(), command.getPageSize());
        var notes = noteRepository.findAllByUserIdWithViewerId(command.getUserId(), loggedUserAccessor.getCurrentUserId(), pageable);
        var mapNotePermission = noteRepository.findAllByUserIdWithViewerIdCanEdit(command.getUserId(), loggedUserAccessor.getCurrentUserId()).stream()
                .collect(Collectors.toMap(NoteEditableProjection::getId, NoteEditableProjection::getCanEdit));

        var notesDto = notes.getContent().stream()
                .map(note -> {
                    var noteDto = modelMapper.map(note, NoteDto.class);
                    noteDto.setIsEditable(Optional.ofNullable(mapNotePermission.get(note.getId())).map(m -> m == 1).orElse(false));
                    return noteDto;
                }).toList();
        var result = new GetNoteResult();
        result.setData(notesDto);
        MappingUtil.mapPageQueryMetadata(notes, result);

        return result;
    }
}