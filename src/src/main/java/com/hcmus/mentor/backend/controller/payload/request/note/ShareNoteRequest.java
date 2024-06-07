package com.hcmus.mentor.backend.controller.payload.request.note;

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

    private List<NoteUserShareRequest> users;
}