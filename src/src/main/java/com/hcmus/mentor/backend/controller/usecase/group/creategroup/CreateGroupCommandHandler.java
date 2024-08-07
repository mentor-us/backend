package com.hcmus.mentor.backend.controller.usecase.group.creategroup;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.payload.ReturnCodeConstants;
import com.hcmus.mentor.backend.domain.AuditRecord;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.GroupUser;
import com.hcmus.mentor.backend.domain.constant.ActionType;
import com.hcmus.mentor.backend.domain.constant.ChannelStatus;
import com.hcmus.mentor.backend.domain.constant.ChannelType;
import com.hcmus.mentor.backend.domain.constant.DomainType;
import com.hcmus.mentor.backend.domainservice.GroupDomainService;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.GroupCategoryRepository;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.AuditRecordService;
import com.hcmus.mentor.backend.service.GroupService;
import com.hcmus.mentor.backend.service.PermissionService;
import com.hcmus.mentor.backend.service.UserService;
import com.hcmus.mentor.backend.service.dto.GroupServiceDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import static com.hcmus.mentor.backend.controller.payload.ReturnCodeConstants.INVALID_PERMISSION;
import static com.hcmus.mentor.backend.controller.payload.ReturnCodeConstants.SUCCESS;

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
    private final AuditRecordService auditRecordService;

    /**
     * @param command The command to create a new group.
     * @return The group service DTO.
     */
    @Override
    @Transactional
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
            return new GroupServiceDto(ReturnCodeConstants.GROUP_GROUP_CATEGORY_NOT_FOUND, "Group category not found", null);
        }

        for (var email : command.getMentorEmails()) {
            if (userService.isAccountActivate(email) == 0) {
                return new GroupServiceDto(ReturnCodeConstants.GROUP_USER_NOT_ACTIVATE, String.format("Tài khoản mentor %s đã bị khoá, xin dùng tài khoản khác", email), null);
            }
        }

        for (var email : command.getMenteeEmails()) {
            if (userService.isAccountActivate(email) == 0) {
                return new GroupServiceDto(ReturnCodeConstants.GROUP_USER_NOT_ACTIVATE, String.format("Tài khoản mentee %s đã bị khoá, xin dùng tài khoản khác", email), null);
            }
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

        Set<GroupUser> groupUsers = new HashSet<>();
        Group finalGroup = group;
        groupUsers.addAll(command.getMentorEmails().stream()
                .filter(Objects::nonNull)
                .filter(Predicate.not(String::isEmpty))
                .map(email -> GroupUser.builder()
                        .user(userService.importUser(email, command.getName()))
                        .group(finalGroup)
                        .isMentor(true).build())
                .toList());
        groupUsers.addAll(command.getMenteeEmails().stream()
                .filter(Objects::nonNull)
                .filter(Predicate.not(String::isEmpty))
                .map(email -> GroupUser.builder()
                        .user(userService.importUser(email, command.getName()))
                        .group(finalGroup)
                        .isMentor(false).build())
                .toList());
        group.setGroupUsers(groupUsers);
        group = groupRepository.save(group);

        var channel = Channel.builder()
                .creator(creator)
                .group(group)
                .name("Cuộc trò chuyện chung")
                .description("Cuộc trò chuyện chung của nhóm")
                .type(ChannelType.PUBLIC)
                .status(ChannelStatus.ACTIVE)
                .users(groupUsers.stream().map(GroupUser::getUser).toList())
                .build();
        group.setDefaultChannel(channel);

        groupRepository.save(group);
        var menteesEmail = new StringBuffer();
        var mentorsEmail = new StringBuffer();
        command.getMenteeEmails().forEach(mentee -> menteesEmail.append("\n").append(mentee));
        command.getMentorEmails().forEach(mentor -> mentorsEmail.append("\n").append(mentor));

        auditRecordService.save(AuditRecord.builder()
                .user(creator)
                .action(ActionType.CREATED)
                .domain(DomainType.GROUP)
                .entityId(group.getId())
                .detail(String.format("Tạo nhóm mới %s: \n Mentors: %s \n Mentees: %s", group.getName(), mentorsEmail, menteesEmail))
                .build());

        logger.info("CreateGroupHandler: Create new group with name {}, category {}, status {}, timeStart {}, timeEnd {}, duration {}, size {}",
                group.getName(), groupCategory.getName(), groupStatus, timeStart, timeEnd, duration, groupUsers.size());

        return new GroupServiceDto(SUCCESS, null, group);
    }

    private GroupServiceDto validate(CreateGroupCommand command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        if (!permissionService.isAdmin(currentUserId, 0)) {
            return new GroupServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }

        var isValidTimeRange = groupDomainService.isStartAndEndTimeValid(command.getTimeStart(), command.getTimeEnd());
        if (!isValidTimeRange) {
            return new GroupServiceDto(ReturnCodeConstants.GROUP_INVALID_DOMAINS, "Invalid time range", null);
        }

        if (groupRepository.existsByName(command.getName())) {
            return new GroupServiceDto(ReturnCodeConstants.GROUP_DUPLICATE_GROUP, "Group name already exists", null);
        }

        var isValidateMemberEmail = groupService.validateListMentorsMentees(command.getMentorEmails(), command.getMenteeEmails());
        if (!Objects.equals(isValidateMemberEmail.getReturnCode(), SUCCESS)) {
            return isValidateMemberEmail;
        }

        return null;
    }
}