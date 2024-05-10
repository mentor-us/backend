package com.hcmus.mentor.backend.controller.usecase.group.creategroup;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.GroupUser;
import com.hcmus.mentor.backend.domain.constant.ChannelStatus;
import com.hcmus.mentor.backend.domain.constant.ChannelType;
import com.hcmus.mentor.backend.domainservice.GroupDomainService;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.GroupCategoryRepository;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.GroupService;
import com.hcmus.mentor.backend.service.PermissionService;
import com.hcmus.mentor.backend.service.UserService;
import com.hcmus.mentor.backend.service.dto.GroupServiceDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static com.hcmus.mentor.backend.controller.payload.returnCode.GroupReturnCode.DUPLICATE_GROUP;
import static com.hcmus.mentor.backend.controller.payload.returnCode.GroupReturnCode.GROUP_CATEGORY_NOT_FOUND;
import static com.hcmus.mentor.backend.controller.payload.returnCode.InvalidPermissionCode.INVALID_PERMISSION;
import static com.hcmus.mentor.backend.controller.payload.returnCode.SuccessCode.SUCCESS;

/**
 * Handler for {@link CreateGroupCommand}.
 */
@Component
@RequiredArgsConstructor
public class CreateGroupCommandHandler implements Command.Handler<CreateGroupCommand, GroupServiceDto> {

    private final Logger logger = LoggerFactory.getLogger(CreateGroupCommandHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final PermissionService permissionService;
    private final GroupService groupService;
    private final GroupRepository groupRepository;
    private final GroupCategoryRepository groupCategoryRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final GroupDomainService groupDomainService;

    /**
     * @param command The command to create a new group.
     * @return The group service DTO.
     */
    @Override
    public GroupServiceDto handle(CreateGroupCommand command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        GroupServiceDto groupServiceDto = validate(command);
        if (groupServiceDto != null) {
            return groupServiceDto;
        }

        var creator = userRepository.findById(currentUserId).orElse(null);
        if (creator == null) {
            return new GroupServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }

        var groupCategory = groupCategoryRepository.findById(command.getGroupCategory()).orElse(null);
        if (groupCategory == null) {
            return new GroupServiceDto(GROUP_CATEGORY_NOT_FOUND, "Group category not found", null);
        }

        var group = modelMapper.map(command, Group.class);

        group.setCreator(creator);
        group.setGroupCategory(groupCategory);
        group.setImageUrl(groupCategory.getIconUrl());

        var timeStart = command.getTimeStart().with(LocalTime.of(0, 0, 0));
        var timeEnd = command.getTimeEnd().with(LocalTime.of(23, 59, 59));
        var duration = Duration.between(timeStart, timeEnd);
        var groupStatus = groupDomainService.getGroupStatus(timeStart, timeEnd);

        group.setTimeStart(timeStart);
        group.setTimeEnd(timeEnd);
        group.setDuration(duration);
        group.setStatus(groupStatus);

        groupRepository.save(group);

        List<GroupUser> groupUsers = new ArrayList<>();
        var mentors = command.getMentorEmails().stream()
                .filter(Objects::nonNull)
                .filter(Predicate.not(String::isEmpty))
                .map(email -> userService.importUser(email, command.getName())).toList();
        var mentees = command.getMenteeEmails().stream()
                .filter(Objects::nonNull)
                .filter(Predicate.not(String::isEmpty))
                .map(email -> userService.importUser(email, command.getName())).toList();

        Group finalGroup = group;
        groupUsers.addAll(mentors.stream().map(user -> GroupUser.builder().user(user).group(finalGroup).isMentor(true).build()).toList());
        groupUsers.addAll(mentees.stream().map(user -> GroupUser.builder().user(user).group(finalGroup).isMentor(false).build()).toList());
        group.setGroupUsers(groupUsers);

        group = groupRepository.save(group);

        var channel = channelRepository.save(Channel.builder()
                .creator(creator)
                .group(group)
                .name("Kênh chung")
                .description("Kênh chung của nhóm")
                .type(ChannelType.PUBLIC)
                .status(ChannelStatus.ACTIVE)
                .users(groupUsers.stream().map(GroupUser::getUser).toList())
                .build());
        group.setDefaultChannel(channel);

        groupRepository.save(group);

        logger.info("CreateGroupHandler: Create new group with name {}, category {}, status {}, timeStart {}, timeEnd {}, duration {}, size {}", group.getName(), groupCategory.getName(), groupStatus, timeStart, timeEnd, duration, groupUsers.size());

        return new GroupServiceDto(SUCCESS, null, group);
    }

    private GroupServiceDto validate(CreateGroupCommand command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        if (!permissionService.isAdmin(currentUserId, 0)) {
            return new GroupServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }

        GroupServiceDto isValidTimeRange = groupService.validateTimeRange(command.getTimeStart(), command.getTimeEnd());
        if (!Objects.equals(isValidTimeRange.getReturnCode(), SUCCESS)) {
            return isValidTimeRange;
        }

        if (groupRepository.existsByName(command.getName())) {
            return new GroupServiceDto(DUPLICATE_GROUP, "Group name already exists", null);
        }

        var isValidateMemberEmail = groupService.validateListMentorsMentees(command.getMentorEmails(), command.getMenteeEmails());
        if (!Objects.equals(isValidateMemberEmail.getReturnCode(), SUCCESS)) {
            return isValidateMemberEmail;
        }

        return null;
    }
}