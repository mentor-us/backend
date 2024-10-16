package com.hcmus.mentor.backend.controller.usecase.grade.common;

import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeVersionDto {

    private String id;
    private String name;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private ShortProfile creator;
    private ShortProfile user;
}