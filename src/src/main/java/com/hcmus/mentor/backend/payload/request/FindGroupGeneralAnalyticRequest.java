package com.hcmus.mentor.backend.payload.request;

import com.hcmus.mentor.backend.entity.Group;
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
    private Group.Status status;
    private Date timeStart;
    private Date timeEnd;
}
