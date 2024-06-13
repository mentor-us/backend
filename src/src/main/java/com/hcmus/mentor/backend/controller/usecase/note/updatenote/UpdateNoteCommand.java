package com.hcmus.mentor.backend.controller.usecase.note.updatenote;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteDetailDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateNoteCommand implements Command<NoteDetailDto> {

    private String noteId;

    private String title;

    private String content;
}