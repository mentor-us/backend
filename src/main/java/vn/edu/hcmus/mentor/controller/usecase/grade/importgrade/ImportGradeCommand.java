package vn.edu.hcmus.mentor.controller.usecase.grade.importgrade;

import an.awesome.pipelinr.Command;
import vn.edu.hcmus.mentor.controller.usecase.grade.common.GradeDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportGradeCommand implements Command<List<GradeDto>> {

    private String userId;
    private MultipartFile file;
}