package com.hcmus.mentor.backend.controller.usecase.group.updategroupbyid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
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

        if (groupRepository.existsByName(command.getName())) {
            throw new DomainException("Tên nhóm đã tồn tại");
        }

        var group = groupRepository.findById(command.getId()).orElseThrow(() -> new DomainException("Không tìm thấy nhóm với id " + command.getId()));

        var groupCategory = groupCategoryRepository.findById(command.getGroupCategory()).orElseThrow(() -> new DomainException("Không tìm thấy loại nhóm với id " + command.getGroupCategory()));

        if (!groupDomainService.isStartAndEndTimeValid(command.getTimeStart(), command.getTimeEnd())) {
            throw new DomainException("Thời gian không hợp lệ");
        }

        if (!command.getName().equals(group.getName())) {
            group.setName(group.getName());
        }
        if (!command.getName().equals(group.getName())) {
            group.setName(group.getName());
        }
        if (!command.getDescription().equals(group.getDescription())) {
            group.setDescription(group.getDescription());
        }
        if (!command.getTimeStart().equals(group.getTimeStart())) {
            group.setTimeStart(group.getTimeStart());
        }
        if (!command.getTimeEnd().equals(group.getTimeEnd())) {
            group.setTimeEnd(group.getTimeEnd());
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

        groupRepository.save(group);

        logger.info("User id {} updated group {}", currentUserId, group.getId());

        return modelMapper.map(group, GroupDetailDto.class);
    }
}