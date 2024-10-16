package com.hcmus.mentor.backend.controller.usecase.group.creategroup;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.payload.ReturnCodeConstants;
import com.hcmus.mentor.backend.domain.AuditRecord;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.GroupUser;
import com.hcmus.mentor.backend.domain.constant.*;
import com.hcmus.mentor.backend.domainservice.GroupDomainService;
import com.hcmus.mentor.backend.repository.GroupCategoryRepository;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.AuditRecordService;
import com.hcmus.mentor.backend.service.PermissionService;
import com.hcmus.mentor.backend.service.UserService;
import com.hcmus.mentor.backend.service.dto.GroupServiceDto;
import com.hcmus.mentor.backend.util.MailUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final GroupRepository groupRepository;
    private final GroupCategoryRepository groupCategoryRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final GroupDomainService groupDomainService;
    private final AuditRecordService auditRecordService;
    private final MailUtils mailUtils;

    /**
     * @param command The command to create a new group.
     * @return The group service DTO.
     */
    @Override
    @Transactional
    public GroupServiceDto handle(CreateGroupCommand command) {
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

        // Validate emails
        var mentorEmails = command.getMentorEmails().stream().filter(StringUtils::isNotBlank).toList();
        var menteeEmails = command.getMenteeEmails().stream().filter(StringUtils::isNotBlank).toList();
        var invalidEmails = Stream.concat(mentorEmails.stream(), menteeEmails.stream())
                .filter(email -> !MailUtils.isValidEmail(email))
                .toList();
        if (!invalidEmails.isEmpty()) {
            return new GroupServiceDto(ReturnCodeConstants.GROUP_INVALID_EMAILS, "Invalid emails", invalidEmails);
        }

        var invalidDomainEmails = Stream.concat(mentorEmails.stream(), menteeEmails.stream())
                .filter(email -> !mailUtils.isValidDomain(email))
                .toList();
        if (!invalidDomainEmails.isEmpty()) {
            return new GroupServiceDto(ReturnCodeConstants.GROUP_INVALID_DOMAINS, "Invalid domains", invalidEmails);
        }

        var duplicateEmails = Stream.concat(mentorEmails.stream(), menteeEmails.stream())
                .collect(Collectors.groupingBy(email -> email, Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .toList();
        if (!duplicateEmails.isEmpty()) {
            return new GroupServiceDto(ReturnCodeConstants.GROUP_DUPLICATE_EMAIL, "Duplicate emails", invalidEmails);
        }

        // validate active account
        var mentorEmailInactive = mentorEmails.stream()
                .filter(email -> userService.isAccountActivate(email) == UserStatus.INACTIVE)
                .toList();
        var menteeEmailInactive = menteeEmails.stream()
                .filter(email -> userService.isAccountActivate(email) == UserStatus.INACTIVE)
                .toList();

        if (!mentorEmailInactive.isEmpty() || !menteeEmailInactive.isEmpty()) {
            String message = "Tài khoản bị khoá bị khoá, vui lòng dùng tài khoản khác:";
            if (!mentorEmailInactive.isEmpty()) {
                message += "\nMentor: " + String.join("\n\t", mentorEmailInactive) + " ";
            }
            if (!menteeEmailInactive.isEmpty()) {
                message += "\nMentee: " + String.join("\n\t", menteeEmailInactive) + " ";
            }
            return new GroupServiceDto(ReturnCodeConstants.GROUP_USER_NOT_ACTIVATE, message, Stream.concat(mentorEmailInactive.stream(), menteeEmailInactive.stream()).toList());
        }

        var groupCategory = groupCategoryRepository.findById(command.getGroupCategory()).orElse(null);
        if (groupCategory == null) {
            return new GroupServiceDto(ReturnCodeConstants.GROUP_GROUP_CATEGORY_NOT_FOUND, "Group category not found", null);
        }

        var group = modelMapper.map(command, Group.class);
        var creator = userRepository.findById(currentUserId).orElse(null);
        var timeStart = command.getTimeStart().with(LocalTime.of(0, 0, 0));
        var timeEnd = command.getTimeEnd().with(LocalTime.of(23, 59, 59));
        var duration = Duration.between(timeStart, timeEnd);
        var groupStatus = groupDomainService.getGroupStatus(timeStart, timeEnd);

        group.setCreator(creator);
        group.setGroupCategory(groupCategory);
        group.setImageUrl(groupCategory.getIconUrl());
        group.setTimeStart(timeStart);
        group.setTimeEnd(timeEnd);
        group.setDuration(duration);
        group.setStatus(groupStatus);

        group = groupRepository.save(group);

        Set<GroupUser> groupUsers = new HashSet<>();
        Group finalGroup = group;
        groupUsers.addAll(mentorEmails.stream()
                .map(email -> GroupUser.builder()
                        .user(userService.importUser(email))
                        .group(finalGroup)
                        .isMentor(true).build())
                .toList());
        groupUsers.addAll(menteeEmails.stream()
                .map(email -> GroupUser.builder()
                        .user(userService.importUser(email))
                        .group(finalGroup)
                        .isMentor(false).build())
                .toList());
        group.setGroupUsers(groupUsers);

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

        auditRecordService.save(AuditRecord.builder()
                .user(creator)
                .action(ActionType.CREATED)
                .domain(DomainType.GROUP)
                .entityId(group.getId())
                .detail(String.format("Tạo nhóm mới %s: \n Mentors: %s \n Mentees: %s", group.getName(),
                        String.join("\n", mentorEmails),
                        String.join("\n", menteeEmails)))
                .build());

        logger.info("CreateGroupHandler: Create new group with name {}, category {}, status {}, timeStart {}, timeEnd {}, duration {}, size {}",
                group.getName(), groupCategory.getName(), groupStatus, timeStart, timeEnd, duration, groupUsers.size());

        return new GroupServiceDto(SUCCESS, null, group);
    }
}