package com.hcmus.mentor.backend.controller.usecase.grade.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeUserProfile {

    private String id;
    private String name;
    private String email;
    private String imageUrl;
}