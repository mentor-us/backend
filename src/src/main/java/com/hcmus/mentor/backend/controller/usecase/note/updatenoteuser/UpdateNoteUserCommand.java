package com.hcmus.mentor.backend.controller.usecase.note.updatenoteuser;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteDetailDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNoteUserCommand implements Command<NoteDetailDto> {

    private String noteId;

    private List<String> userIds;
}