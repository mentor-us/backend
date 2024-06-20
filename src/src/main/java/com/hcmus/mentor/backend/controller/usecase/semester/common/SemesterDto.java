package com.hcmus.mentor.backend.controller.usecase.semester.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemesterDto {

    private String id;
    private String name;
}