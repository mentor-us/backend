package com.hcmus.mentor.backend.controller.usecase.group.creategroup;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.GroupUser;
import com.hcmus.mentor.backend.domain.constant.ChannelStatus;
import com.hcmus.mentor.backend.domain.constant.ChannelType;
import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.GroupCategoryRepository;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.service.GroupService;
import com.hcmus.mentor.backend.service.PermissionService;
import com.hcmus.mentor.backend.service.UserService;
import com.hcmus.mentor.backend.service.dto.GroupServiceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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

    private final PermissionService permissionService;
    private final GroupService groupService;
    private final GroupRepository groupRepository;
    private final GroupCategoryRepository groupCategoryRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    /**
     *
     * @param command The command to create a new group.
     * @return The group service DTO.
     */
    @Override
    public GroupServiceDto handle(final CreateGroupCommand command) {
        if (!permissionService.isAdmin(command.getCreatorEmail())) {
            return new GroupServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }

        GroupServiceDto isValidTimeRange = groupService.validateTimeRange(command.getRequest().getTimeStart(), command.getRequest().getTimeEnd());
        if (!Objects.equals(isValidTimeRange.getReturnCode(), SUCCESS)) {
            return isValidTimeRange;
        }

        if (groupRepository.existsByName(command.getRequest().getName())) {
            return new GroupServiceDto(DUPLICATE_GROUP, "Group name already exists", null);
        }

        var groupCategory = groupCategoryRepository.findById(command.getRequest().getGroupCategory()).orElse(null);
        if (groupCategory == null) {
            return new GroupServiceDto(GROUP_CATEGORY_NOT_FOUND, "Group category not found", null);
        }

        var isValidateMemberEmail = groupService.validateListMentorsMentees(command.getRequest().getMentorEmails(), command.getRequest().getMenteeEmails());
        if (!Objects.equals(isValidateMemberEmail.getReturnCode(), SUCCESS)) {
            return isValidateMemberEmail;
        }

        Date timeStart = groupService.changeGroupTime(command.getRequest().getTimeStart(), "START");
        Date timeEnd = groupService.changeGroupTime(command.getRequest().getTimeEnd(), "END");
        Duration duration = groupService.calculateDuration(timeStart, timeEnd);
        GroupStatus status = groupService.getStatusFromTimeStartAndTimeEnd(timeStart, timeEnd);

        var creator = userRepository.findByEmail(command.getCreatorEmail()).orElse(null);
        if (creator == null) {
            return new GroupServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }

        var group = groupRepository.save(Group.builder()
                .name(command.getRequest().getName())
                .description(command.getRequest().getDescription())
                .createdDate(new Date())
                .timeStart(timeStart)
                .timeEnd(timeEnd)
                .duration(duration)
                .status(status)
                .groupCategory(groupCategory)
                .creator(creator)
                .build());



        groupRepository.save(group);

        List<GroupUser> groupUsers = new ArrayList<>();
        var mentors = command.getRequest().getMentorEmails().stream()
                .filter(email -> !email.isEmpty())
                .map(email -> userService.importUser(email, command.getRequest().getName())).toList();
        var mentees = command.getRequest().getMenteeEmails().stream()
                .filter(email -> !email.isEmpty())
                .map(email -> userService.importUser(email, command.getRequest().getName())).toList();
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

        return new GroupServiceDto(SUCCESS, null, group);
    }
}