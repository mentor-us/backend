package com.hcmus.mentor.backend.controller.usecase.note.common;

import lombok.*;

/**
 * Short profile of a note
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoteUserProfile {

    private String id;

    private String name;

    private String email;

    private String imageUrl;

    private Long totalNotes;
}