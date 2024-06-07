package com.hcmus.mentor.backend.controller.usecase.note.createnote;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteDetailDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Command for creating a note.
 */
@Data
@Builder
public class CreateNoteCommand implements Command<NoteDetailDto> {

        private String creatorId;

        private String title;

        private String content;

//        private boolean isPublic;

        private List<String> userIds;
}