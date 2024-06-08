package com.hcmus.mentor.backend.controller.usecase.note.createnote;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteDetailDto;
import com.hcmus.mentor.backend.domain.Note;
import com.hcmus.mentor.backend.repository.NoteRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.HashSet;

/**
 * Handler for
 */
@Component
@RequiredArgsConstructor
public class CreateNoteCommandHandler implements Command.Handler<CreateNoteCommand, NoteDetailDto> {

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final ModelMapper modelMapper;

    @Override
    public NoteDetailDto handle(CreateNoteCommand command) {
        var creator = userRepository.findById(command.getCreatorId())
                .orElseThrow(() -> new DomainException("Người tạo ghi chú không tồn tại"));
        var users = userRepository.findAllById(command.getUserIds());
       Note note =  noteRepository.save(Note.builder()
                .title(command.getTitle())
                .content(command.getContent())
                .creator(creator)
                .owner(creator)
                .users(new HashSet<>(users))
                .build());
        return modelMapper.map(note, NoteDetailDto.class);
    }
}