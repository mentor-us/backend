package vn.edu.hcmus.mentor.backend.controller.payload.request.note;

import vn.edu.hcmus.mentor.backend.domain.constant.NoteShareType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareNoteRequest {

    @NotNull(message = "Loại chia sẻ không được trống")
    private NoteShareType shareType;

    private List<NoteUserShareRequest> users;
}