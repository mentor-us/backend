package com.hcmus.mentor.backend.controller.payload.request.note;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNoteRequest {

    @NotBlank(message = "Tiêu đề không được trống")
    private String title;

    @NotBlank(message = "Nội dung không được trống")
    private String content;

    @NotEmpty(message = "Ghi chú phải liên quan tới ít nhất 1 người")
    private List<String> userIds;
}