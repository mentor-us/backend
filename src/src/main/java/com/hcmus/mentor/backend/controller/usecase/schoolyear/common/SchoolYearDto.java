package com.hcmus.mentor.backend.controller.usecase.schoolyear.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolYearDto {

    private String id;
    private String name;
}