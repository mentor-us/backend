package vn.edu.hcmus.mentor.controller.payload.response;

import vn.edu.hcmus.mentor.controller.payload.response.groups.GroupHomepageResponse;
import vn.edu.hcmus.mentor.service.dto.EventDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class HomePageResponse {

    List<EventDto> events;
    List<GroupHomepageResponse> pinnedGroups;
    Slice<GroupHomepageResponse> groups;
}
