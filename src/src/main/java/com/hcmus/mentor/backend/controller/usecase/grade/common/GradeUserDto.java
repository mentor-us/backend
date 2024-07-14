package com.hcmus.mentor.backend.controller.usecase.grade.common;

import com.hcmus.mentor.backend.domain.constant.GradeShareType;
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