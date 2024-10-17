package vn.edu.hcmus.mentor.backend.controller.usecase.grade.getversionsbystudentid;

import an.awesome.pipelinr.Command;
import vn.edu.hcmus.mentor.backend.controller.usecase.grade.common.GradeVersionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetVersionsByStudentIdCommand implements Command<List<GradeVersionDto>> {

    private String studentId;
}