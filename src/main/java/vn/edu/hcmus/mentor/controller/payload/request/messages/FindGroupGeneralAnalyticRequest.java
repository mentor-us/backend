package vn.edu.hcmus.mentor.controller.payload.request.messages;

import vn.edu.hcmus.mentor.domain.constant.GroupStatus;
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