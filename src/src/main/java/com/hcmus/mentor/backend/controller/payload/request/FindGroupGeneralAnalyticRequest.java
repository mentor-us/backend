package com.hcmus.mentor.backend.controller.payload.request;

import com.hcmus.mentor.backend.domain.Group;

import java.util.Date;

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
    private Group.Status status;
    private Date timeStart;
    private Date timeEnd;
}
