package com.hcmus.mentor.backend.controller.usecase.grade.getshareinfobyuserid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.grade.common.GradeUserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetShareInfoByUserQuery implements Command<GradeUserDto> {

    private String userId;
}