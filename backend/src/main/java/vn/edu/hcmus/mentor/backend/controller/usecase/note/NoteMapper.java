package vn.edu.hcmus.mentor.backend.controller.usecase.note;

import vn.edu.hcmus.mentor.backend.controller.payload.request.note.CreateNoteRequest;
import vn.edu.hcmus.mentor.backend.controller.payload.request.note.GetNotesByUserRequest;
import vn.edu.hcmus.mentor.backend.controller.payload.request.note.ShareNoteRequest;
import vn.edu.hcmus.mentor.backend.controller.payload.request.note.UpdateNoteRequest;
import vn.edu.hcmus.mentor.backend.controller.usecase.common.mapper.MapperConverter;
import vn.edu.hcmus.mentor.backend.controller.usecase.note.common.NoteDetailDto;
import vn.edu.hcmus.mentor.backend.controller.usecase.note.common.NoteDto;
import vn.edu.hcmus.mentor.backend.controller.usecase.note.common.NoteHistoryDto;
import vn.edu.hcmus.mentor.backend.controller.usecase.note.common.NoteUserProfile;
import vn.edu.hcmus.mentor.backend.controller.usecase.note.createnote.CreateNoteCommand;
import vn.edu.hcmus.mentor.backend.controller.usecase.note.getnotesbyuserid.GetNotesByUserIdQuery;
import vn.edu.hcmus.mentor.backend.controller.usecase.note.sharenote.ShareNoteCommand;
import vn.edu.hcmus.mentor.backend.controller.usecase.note.updatenote.UpdateNoteCommand;
import vn.edu.hcmus.mentor.backend.domain.Note;
import vn.edu.hcmus.mentor.backend.domain.NoteHistory;
import vn.edu.hcmus.mentor.backend.domain.NoteUserAccess;
import vn.edu.hcmus.mentor.backend.domain.User;
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
            mapper.using(MapperConverter.toLocalDateTime).map(Note::getCreatedDate, NoteDetailDto::setCreatedDate);
            mapper.using(MapperConverter.toLocalDateTime).map(Note::getUpdatedDate, NoteDetailDto::setUpdatedDate);
        });
        modelMapper.createTypeMap(Note.class, NoteDto.class).addMappings(mapper -> {
            mapper.using(MapperConverter.toLocalDateTime).map(Note::getCreatedDate, NoteDto::setCreatedDate);
            mapper.using(MapperConverter.toLocalDateTime).map(Note::getUpdatedDate, NoteDto::setUpdatedDate);
        });

        modelMapper.createTypeMap(NoteHistory.class, NoteHistoryDto.class).addMappings(mapper -> {
            mapper.using(MapperConverter.toLocalDateTime).map(NoteHistory::getCreatedDate, NoteHistoryDto::setCreatedDate);
            mapper.using(MapperConverter.toLocalDateTime).map(NoteHistory::getUpdatedDate, NoteHistoryDto::setUpdatedDate);
        });
        modelMapper.createTypeMap(NoteUserAccess.class, NoteUserProfile.class);
    }
}