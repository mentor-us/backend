package com.hcmus.mentor.backend.controller.usecase.note.common;

import com.hcmus.mentor.backend.domain.constant.NotePermission;
import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteUserAccessDto {

    private NoteUserProfile user;

    private NotePermission notePermission = NotePermission.VIEW;
}