package com.hcmus.mentor.backend.controller.usecase.group.getgroupworkspace;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.payload.response.groups.GroupDetailResponse;
import com.hcmus.mentor.backend.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handler for {@link GetGroupWorkSpaceQuery}.
 */
@Component
@RequiredArgsConstructor
public class GetGroupWorkspaceQueryHandler implements Command.Handler<GetGroupWorkSpaceQuery, GroupDetailResponse> {

    private GroupRepository groupRepository;

    /**
     * @param command command to get group detail.
     * @return result of getting group detail.
     */
    @Override
    public GroupDetailResponse handle(GetGroupWorkSpaceQuery command) {
        var group = groupRepository.findById(command.getGroupId()).orElseThrow(() -> new DomainException("Không tim thấy nhóm"));

        if (group.getGroupUsers().stream().noneMatch(groupUser -> groupUser.getUser().getId().equals(command.getCurrentUserId()))) {
            throw new DomainException("Bạn không thể xem nhóm này");
        }

        return null;
    }
}
