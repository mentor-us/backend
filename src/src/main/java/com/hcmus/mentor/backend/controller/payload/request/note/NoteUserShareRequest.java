package com.hcmus.mentor.backend.controller.payload.request.note;

import com.hcmus.mentor.backend.domain.NoteUserAccess;
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

    private NoteUserAccess accessType;
}