package com.hcmus.mentor.backend.controller.usecase.grade.gettemplateimport;

import an.awesome.pipelinr.Command;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class GetTemplateImportGradeQuery implements Command<GetTemplateImportGradeResult> {
}
