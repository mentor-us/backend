package com.hcmus.mentor.backend.controller.usecase.group.findowngroups;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.payload.response.groups.GroupHomepageResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

/**
 * Command to find own groups.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FindOwnGroupsCommand implements Command<Page<GroupHomepageResponse>> {
    private  String userId;
    private Boolean isMentor;
    private  int page;
    private  int pageSize;
}
