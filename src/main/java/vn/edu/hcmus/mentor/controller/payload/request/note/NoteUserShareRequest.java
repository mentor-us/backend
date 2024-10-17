package vn.edu.hcmus.mentor.controller.payload.request.note;

import vn.edu.hcmus.mentor.domain.constant.NotePermission;
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