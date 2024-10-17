package vn.edu.hcmus.mentor.controller.usecase.note.common;

import vn.edu.hcmus.mentor.domain.constant.NotePermission;
import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteUserAccessDto {

    private NoteUserProfile user;

    private NotePermission notePermission = NotePermission.VIEW;
}