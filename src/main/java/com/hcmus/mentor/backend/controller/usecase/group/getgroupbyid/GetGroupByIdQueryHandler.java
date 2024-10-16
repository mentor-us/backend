package com.hcmus.mentor.backend.controller.usecase.group.getgroupbyid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.usecase.group.common.GroupDetailDto;
import com.hcmus.mentor.backend.domain.constant.GroupCategoryPermission;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Handler for {@link GetGroupByIdQueryHandler}.
 */
@Component
@RequiredArgsConstructor
public class GetGroupByIdQueryHandler implements Command.Handler<GetGroupByIdQuery, GroupDetailDto> {

    private final ModelMapper modelMapper;
    private final GroupRepository groupRepository;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    public GroupDetailDto handle(GetGroupByIdQuery query) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();
        var group = groupRepository.findById(query.getId()).orElseThrow(() -> new DomainException("Không tìm thấy nhóm với id " + query.getId()));
        var groupCategory = group.getGroupCategory();

        GroupDetailDto groupDetailDto = modelMapper.map(group, GroupDetailDto.class);
        groupDetailDto.setPermissions(groupCategory.getPermissions().stream().filter(c-> Objects.equals(c.getDescription(), GroupCategoryPermission.FAQ_MANAGEMENT.getDescription())).toList());
        groupDetailDto.setRole(currentUserId);
        if (query.isDetail()) {
            groupDetailDto.setGroupCategory(group.getGroupCategory().getName());
        }

        return groupDetailDto;
    }
}