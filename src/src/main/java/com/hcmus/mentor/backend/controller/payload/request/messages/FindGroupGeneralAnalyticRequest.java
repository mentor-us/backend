package com.hcmus.mentor.backend.controller.payload.request.messages;

import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

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