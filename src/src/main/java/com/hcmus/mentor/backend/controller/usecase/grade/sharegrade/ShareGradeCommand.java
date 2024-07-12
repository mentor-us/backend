package com.hcmus.mentor.backend.controller.usecase.grade.sharegrade;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.grade.common.GradeUserDto;
import com.hcmus.mentor.backend.domain.constant.GradeShareType;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareGradeCommand implements Command<GradeUserDto> {
    @NonNull
    private String userId;

    @NonNull
    private List<String> userAccessIds;

    @NonNull
    private GradeShareType shareType;
}