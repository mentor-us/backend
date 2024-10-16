package com.hcmus.mentor.backend.controller.payload.request.note;

import com.hcmus.mentor.backend.domain.constant.NotePermission;
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