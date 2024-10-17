package vn.edu.hcmus.mentor.controller.usecase.grade.getshareinfobyuserid;

import an.awesome.pipelinr.Command;
import vn.edu.hcmus.mentor.controller.usecase.grade.common.GradeUserDto;
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