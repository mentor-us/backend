package com.hcmus.mentor.backend.controller.usecase.group.getgroupworkspace;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.payload.response.groups.GroupDetailResponse;
import lombok.*;

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
