package com.hcmus.mentor.backend.controller.usecase.note.common;

import com.hcmus.mentor.backend.domain.NoteHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoteDetailDto {

    private String id;

    private String title;

    private String content;

    private NoteUserProfile creator;

    private NoteUserProfile updatedBy;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    private boolean isEditable;

    private boolean isPublic;

    @Builder.Default
    private List<NoteHistory> noteHistories = new ArrayList<>();

    @Builder.Default
    private List<NoteUserProfile> users = new ArrayList<>();

    @Builder.Default
    private List<NoteUserProfile> userAccesses = new ArrayList<>();
}