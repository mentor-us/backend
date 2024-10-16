package com.hcmus.mentor.backend.controller.usecase.group.gethomepage;

import com.hcmus.mentor.backend.controller.payload.response.groups.GroupHomepageResponse;
import com.hcmus.mentor.backend.controller.usecase.common.NewEventDto;
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
