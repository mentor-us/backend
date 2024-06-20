package com.hcmus.mentor.backend.controller.usecase.note.common;

import com.hcmus.mentor.backend.domain.constant.NoteShareType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoteDto {

    private String id;

    private String title;

    private String content;

    private NoteUserProfile creator;

    private NoteShareType shareType;

    private NoteUserProfile owner;

    private NoteUserProfile updatedBy;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    private Boolean isEditable;
}