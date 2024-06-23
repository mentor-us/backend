package com.hcmus.mentor.backend.controller.usecase.note;

import com.hcmus.mentor.backend.controller.payload.request.note.CreateNoteRequest;
import com.hcmus.mentor.backend.controller.payload.request.note.GetNotesByUserRequest;
import com.hcmus.mentor.backend.controller.payload.request.note.ShareNoteRequest;
import com.hcmus.mentor.backend.controller.payload.request.note.UpdateNoteRequest;
import com.hcmus.mentor.backend.controller.usecase.common.mapper.MapperConverter;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteDetailDto;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteDto;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteHistoryDto;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteUserProfile;
import com.hcmus.mentor.backend.controller.usecase.note.createnote.CreateNoteCommand;
import com.hcmus.mentor.backend.controller.usecase.note.getnotesbyuserid.GetNotesByUserIdQuery;
import com.hcmus.mentor.backend.controller.usecase.note.sharenote.ShareNoteCommand;
import com.hcmus.mentor.backend.controller.usecase.note.updatenote.UpdateNoteCommand;
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
        modelMapper.createTypeMap(UpdateNoteRequest.class, UpdateNoteCommand.class).addMappings(mapper -> {
            mapper.skip(UpdateNoteCommand::setNoteId);
        });

        modelMapper.createTypeMap(ShareNoteRequest.class, ShareNoteCommand.class);
        modelMapper.createTypeMap(GetNotesByUserRequest.class, GetNotesByUserIdQuery.class);
        modelMapper.createTypeMap(User.class, NoteUserProfile.class);
        modelMapper.createTypeMap(Note.class, NoteDetailDto.class).addMappings(mapper -> {
            mapper.using(MapperConverter.mapDateConverter()).map(Note::getCreatedDate, NoteDetailDto::setCreatedDate);
            mapper.using(MapperConverter.mapDateConverter()).map(Note::getUpdatedDate, NoteDetailDto::setUpdatedDate);
        });
        modelMapper.createTypeMap(Note.class, NoteDto.class).addMappings(mapper -> {
            mapper.using(MapperConverter.mapDateConverter()).map(Note::getCreatedDate, NoteDto::setCreatedDate);
            mapper.using(MapperConverter.mapDateConverter()).map(Note::getUpdatedDate, NoteDto::setUpdatedDate);
        });

        modelMapper.createTypeMap(NoteHistory.class, NoteHistoryDto.class).addMappings(mapper -> {
            mapper.using(MapperConverter.mapDateConverter()).map(NoteHistory::getCreatedDate, NoteHistoryDto::setCreatedDate);
            mapper.using(MapperConverter.mapDateConverter()).map(NoteHistory::getUpdatedDate, NoteHistoryDto::setUpdatedDate);
        });
        modelMapper.createTypeMap(NoteUserAccess.class, NoteUserProfile.class);
    }
}