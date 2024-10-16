package com.hcmus.mentor.backend.controller.usecase.grade.deleteversion;

import an.awesome.pipelinr.Command;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteVersionCommand implements Command<Void> {

    private String versionId;
}