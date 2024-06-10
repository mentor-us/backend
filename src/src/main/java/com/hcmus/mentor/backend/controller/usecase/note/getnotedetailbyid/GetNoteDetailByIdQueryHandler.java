package com.hcmus.mentor.backend.controller.usecase.note.getnotedetailbyid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteDetailDto;
import com.hcmus.mentor.backend.repository.NoteRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetNoteDetailByIdQueryHandler implements Command.Handler<GetNoteDetailByIdQuery, NoteDetailDto> {

    private final NoteRepository noteRepository;
    private final LoggedUserAccessor loggedUserAccessor;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public NoteDetailDto handle(GetNoteDetailByIdQuery command) {
        var viewer = userRepository.findById(loggedUserAccessor.getCurrentUserId())
                .orElseThrow(() -> new DomainException("Không tìm thấy người đang đăng nhập"));

        var note = noteRepository.getNoteById(command.getNoteId()).orElseThrow(() -> new DomainException("Không tìm thấy ghi chú"));

        if (noteRepository.canView(viewer.getId(), command.getNoteId())) {
            throw new DomainException("Bạn không có quyền xem ghi chú này");
        }

        return modelMapper.map(note, NoteDetailDto.class);
    }
}