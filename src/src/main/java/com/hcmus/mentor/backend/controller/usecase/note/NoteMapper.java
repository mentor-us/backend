package com.hcmus.mentor.backend.controller.usecase.note;

import com.hcmus.mentor.backend.controller.payload.request.note.*;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteDetailDto;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteDto;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteHistoryDto;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteUserProfile;
import com.hcmus.mentor.backend.controller.usecase.note.createnote.CreateNoteCommand;
import com.hcmus.mentor.backend.controller.usecase.note.getnotesbyuserid.GetNotesByUserIdQuery;
import com.hcmus.mentor.backend.controller.usecase.note.sharenote.ShareNoteCommand;
import com.hcmus.mentor.backend.controller.usecase.note.updatenote.UpdateNoteCommand;
import com.hcmus.mentor.backend.controller.usecase.note.updatenoteuser.UpdateNoteUserCommand;
import com.hcmus.mentor.backend.domain.Note;
import com.hcmus.mentor.backend.domain.NoteHistory;
import com.hcmus.mentor.backend.domain.NoteUserAccess;
import com.hcmus.mentor.backend.domain.User;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class NoteMapper {

    public NoteMapper(ModelMapper modelMapper) {
        modelMapper.createTypeMap(CreateNoteRequest.class, CreateNoteCommand.class);
        modelMapper.createTypeMap(UpdateNoteRequest.class, UpdateNoteCommand.class).addMappings(
                mapping -> {
                            mapping.skip(UpdateNoteCommand::setNoteId);
                        }
                );
        modelMapper.createTypeMap(UpdateNoteUserRequest.class, UpdateNoteUserCommand.class);

        modelMapper.createTypeMap(ShareNoteRequest.class, ShareNoteCommand.class);
        modelMapper.createTypeMap(GetNotesByUserRequest.class, GetNotesByUserIdQuery.class);
        modelMapper.createTypeMap(User.class, NoteUserProfile.class);
        modelMapper.createTypeMap(Note.class, NoteDetailDto.class);
        modelMapper.createTypeMap(Note.class, NoteDto.class);

        modelMapper.createTypeMap(NoteHistory.class, NoteHistoryDto.class);
        modelMapper.createTypeMap(NoteUserAccess.class, NoteUserProfile.class);
    }
}