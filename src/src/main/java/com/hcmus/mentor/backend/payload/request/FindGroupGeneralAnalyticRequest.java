package com.hcmus.mentor.backend.payload.request;

import com.hcmus.mentor.backend.entity.Group;
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
