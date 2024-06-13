package com.hcmus.mentor.backend.controller.usecase.note.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteEditable {
    private String noteId;
    private Boolean editor;
}