package com.hcmus.mentor.backend.controller.usecase.group.updategroupbyid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.payload.ReturnCodeConstants;
import com.hcmus.mentor.backend.controller.usecase.group.common.GroupDetailDto;
import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import com.hcmus.mentor.backend.domainservice.GroupDomainService;
import com.hcmus.mentor.backend.repository.GroupCategoryRepository;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handler for {@link UpdateGroupByIdCommandHandler}.
 */
@Component
@RequiredArgsConstructor
public class UpdateGroupByIdCommandHandler implements Command.Handler<UpdateGroupByIdCommand, GroupDetailDto> {

    private final Logger logger = LoggerFactory.getLogger(UpdateGroupByIdCommandHandler.class);
    private final LoggedUserAccessor loggedUserAccessor;
    private final ModelMapper modelMapper;
    private final GroupRepository groupRepository;
    private final GroupCategoryRepository groupCategoryRepository;
    private final GroupDomainService groupDomainService;

    @Override
    public GroupDetailDto handle(UpdateGroupByIdCommand command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var group = groupRepository.findById(command.getId()).orElseThrow(() -> new DomainException("Không tìm thấy nhóm với id " + command.getId(), ReturnCodeConstants.GROUP_NOT_FOUND));

        if (groupRepository.existsByName(command.getName()) && !command.getName().equals(group.getName())) {
            throw new DomainException("Tên nhóm đã tồn tại", ReturnCodeConstants.GROUP_DUPLICATE_GROUP);
        }

        var groupCategory = groupCategoryRepository.findById(command.getGroupCategory()).orElseThrow(() -> new DomainException("Không tìm thấy loại nhóm với id " + command.getGroupCategory(), ReturnCodeConstants.GROUP_NOT_FOUND));

        if (!groupDomainService.isStartAndEndTimeValid(command.getTimeStart(), command.getTimeEnd())) {
            throw new DomainException("Thời gian không hợp lệ", ReturnCodeConstants.GROUP_TIME_START_TOO_FAR_FROM_NOW);
        }

        if (!command.getName().equals(group.getName())) {
            group.setName(command.getName());
        }
        if (!command.getDescription().equals(group.getDescription())) {
            group.setDescription(command.getDescription());
        }
        if (!command.getTimeStart().equals(group.getTimeStart())) {
            group.setTimeStart(command.getTimeStart());
        }
        if (!command.getTimeEnd().equals(group.getTimeEnd())) {
            group.setTimeEnd(command.getTimeEnd());
        }
        if (!command.getStatus().equals(GroupStatus.DISABLED)) {
            var newGroupStatus = groupDomainService.getGroupStatus(command.getTimeStart(), command.getTimeEnd());
            if (newGroupStatus.equals(group.getStatus())) {
                group.setStatus(newGroupStatus);
            }
            if (!groupCategory.equals(group.getGroupCategory())) {
                group.setGroupCategory(groupCategory);
            }
        } else {
            group.setStatus(GroupStatus.DISABLED);
        }

        group = groupRepository.save(group);

        logger.info("User id {} updated group {}", currentUserId, group.getId());

        return modelMapper.map(group, GroupDetailDto.class);
    }
}