package com.hcmus.mentor.backend.controller.usecase.group.getgroupworkspace;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.payload.response.groups.GroupDetailResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Query to get group detail
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetGroupWorkSpaceQuery implements Command<GroupDetailResponse> {
    private String currentUserId;
    private String groupId;
}
