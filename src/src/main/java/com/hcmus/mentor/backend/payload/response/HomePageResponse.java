package com.hcmus.mentor.backend.payload.response;

import com.hcmus.mentor.backend.payload.response.groups.GroupHomepageResponse;
import com.hcmus.mentor.backend.usercase.common.service.impl.EventServiceImpl;
import java.util.List;
import lombok.*;
import org.springframework.data.domain.Slice;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class HomePageResponse {

  List<EventServiceImpl.Event> events;
  List<GroupHomepageResponse> pinnedGroups;
  Slice<GroupHomepageResponse> groups;
}
