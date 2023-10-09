package com.hcmus.mentor.backend.payload.response;

import com.hcmus.mentor.backend.payload.response.groups.GroupHomepageResponse;
import com.hcmus.mentor.backend.service.EventService;
import java.util.List;
import lombok.*;
import org.springframework.data.domain.Slice;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class HomePageResponse {

  List<EventService.Event> events;
  List<GroupHomepageResponse> pinnedGroups;
  Slice<GroupHomepageResponse> groups;
}
