package vn.edu.hcmus.mentor.backend.controller.usecase.note.common;

import vn.edu.hcmus.mentor.backend.domain.constant.NotePermission;
import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteUserAccessDto {

    private NoteUserProfile user;

    private NotePermission notePermission = NotePermission.VIEW;
}