package com.hcmus.mentor.backend.controller.usecase.note;

import com.hcmus.mentor.backend.controller.payload.request.note.CreateNoteRequest;
import com.hcmus.mentor.backend.controller.payload.request.note.ShareNoteRequest;
import com.hcmus.mentor.backend.controller.payload.request.note.UpdateNoteRequest;
import com.hcmus.mentor.backend.controller.usecase.note.createnote.CreateNoteCommand;
import com.hcmus.mentor.backend.controller.usecase.note.sharenote.ShareNoteCommand;
import com.hcmus.mentor.backend.controller.usecase.note.updatenote.UpdateNoteCommand;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class NoteMapper {

    public NoteMapper(ModelMapper modelMapper) {
        modelMapper.createTypeMap(CreateNoteRequest.class, CreateNoteCommand.class);
        modelMapper.createTypeMap(UpdateNoteRequest.class, UpdateNoteCommand.class);
        modelMapper.createTypeMap(ShareNoteRequest.class, ShareNoteCommand.class);
    }
}