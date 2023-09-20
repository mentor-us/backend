package com.hcmus.mentor.backend.payload.response;

import com.hcmus.mentor.backend.payload.response.groups.GroupHomepageResponse;
import com.hcmus.mentor.backend.service.EventService;
import lombok.*;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class HomePageResponse {

    List<EventService.Event> events;
    List<GroupHomepageResponse> pinnedGroups;
    Slice<GroupHomepageResponse> groups;

}
