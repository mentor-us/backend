package com.hcmus.mentor.backend.controller.usecase.note.common;

import com.hcmus.mentor.backend.domain.constant.NoteShareType;
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

    private NoteShareType shareType;

    private NoteUserProfile creator;

    private NoteUserProfile updatedBy;

    private NoteUserProfile owner;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    private boolean isEditable;

    @Builder.Default
    private List<NoteHistoryDto> noteHistories = new ArrayList<>();

    @Builder.Default
    private List<NoteUserProfile> users = new ArrayList<>();

    @Builder.Default
    private List<NoteUserAccessDto> userAccesses = new ArrayList<>();
}