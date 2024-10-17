package vn.edu.hcmus.mentor.backend.controller.usecase.note.sharenote;

import an.awesome.pipelinr.Command;
import vn.edu.hcmus.mentor.backend.controller.payload.request.note.NoteUserShareRequest;
import vn.edu.hcmus.mentor.backend.controller.usecase.note.common.NoteDetailDto;
import vn.edu.hcmus.mentor.backend.domain.constant.NoteShareType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareNoteCommand implements Command<NoteDetailDto> {

    private String noteId;

    private NoteShareType shareType;

    private List<NoteUserShareRequest> users;
}