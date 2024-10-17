package vn.edu.hcmus.mentor.controller.usecase.note.sharenote;

import an.awesome.pipelinr.Command;
import vn.edu.hcmus.mentor.controller.payload.request.note.NoteUserShareRequest;
import vn.edu.hcmus.mentor.controller.usecase.note.common.NoteDetailDto;
import vn.edu.hcmus.mentor.domain.constant.NoteShareType;
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