package vn.edu.hcmus.mentor.backend.controller.usecase.grade.getgradebyid;

import an.awesome.pipelinr.Command;
import vn.edu.hcmus.mentor.backend.controller.usecase.grade.common.GradeDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetGradeByIdQuery implements Command<GradeDto> {

    private String id;
}
