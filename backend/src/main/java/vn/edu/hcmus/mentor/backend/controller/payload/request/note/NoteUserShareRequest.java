package vn.edu.hcmus.mentor.backend.controller.payload.request.note;

import vn.edu.hcmus.mentor.backend.domain.constant.NotePermission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteUserShareRequest {

    private String userId;

    private NotePermission accessType;
}