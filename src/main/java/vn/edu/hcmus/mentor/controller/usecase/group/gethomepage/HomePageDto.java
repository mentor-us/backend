package vn.edu.hcmus.mentor.controller.usecase.group.gethomepage;

import vn.edu.hcmus.mentor.controller.payload.response.groups.GroupHomepageResponse;
import vn.edu.hcmus.mentor.controller.usecase.common.NewEventDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomePageDto {

    List<NewEventDto> events;
    List<GroupHomepageResponse> pinnedGroups;
    Slice<GroupHomepageResponse> groups;
}
