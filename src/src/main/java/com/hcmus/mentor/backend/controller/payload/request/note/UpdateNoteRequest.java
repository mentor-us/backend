package com.hcmus.mentor.backend.controller.payload.request.note;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNoteRequest {

    private String title;

    private String content;
}