package com.hcmus.mentor.backend.controller.usecase.grade.gettemplateimport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.ByteArrayInputStream;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetTemplateImportGradeResult {

    private ByteArrayInputStream stream;
    private long contentLength;
    
    @Builder.Default
    private String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
}
