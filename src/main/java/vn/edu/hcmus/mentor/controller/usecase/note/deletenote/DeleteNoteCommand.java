package vn.edu.hcmus.mentor.controller.usecase.note.deletenote;

import an.awesome.pipelinr.Command;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteNoteCommand implements Command<Void> {

    private String noteId;
}