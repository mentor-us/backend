package com.hcmus.mentor.backend.controller.payload.request;

import java.util.Date;

import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FindGroupGeneralAnalyticRequest {
    private String groupName;
    private String groupCategory;
    private GroupStatus status;
    private Date timeStart;
    private Date timeEnd;
}
