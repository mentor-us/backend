package com.hcmus.mentor.backend.controller.usecase.note.common;

import com.hcmus.mentor.backend.domain.NoteHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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

    private List<NoteHistory> noteHistories;

    private List<NoteUserProfile> users;

    private List<NoteUserProfile> userAccesses;
}