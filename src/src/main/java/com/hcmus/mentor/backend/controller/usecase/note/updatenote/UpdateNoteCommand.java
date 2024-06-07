package com.hcmus.mentor.backend.controller.usecase.note.updatenote;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteDetailDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UpdateNoteCommand implements Command<NoteDetailDto> {

    private String editorId;

    private String id;

    private String title;

    private String content;

    private List<String> userIds;
}