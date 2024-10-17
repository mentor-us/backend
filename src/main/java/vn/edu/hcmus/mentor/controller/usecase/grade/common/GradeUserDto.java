package vn.edu.hcmus.mentor.controller.usecase.grade.common;

import vn.edu.hcmus.mentor.domain.constant.GradeShareType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeUserDto {

    private String userId;
    private GradeShareType shareType;
    private List<GradeUserProfile> userAccesses;
}