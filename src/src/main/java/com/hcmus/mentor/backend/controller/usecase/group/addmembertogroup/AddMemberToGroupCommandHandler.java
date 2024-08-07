package com.hcmus.mentor.backend.controller.usecase.group.addmembertogroup;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
import com.hcmus.mentor.backend.controller.usecase.group.common.GroupDetailDto;
import com.hcmus.mentor.backend.domain.AuditRecord;
import com.hcmus.mentor.backend.domain.GroupUser;
import com.hcmus.mentor.backend.domain.constant.ActionType;
import com.hcmus.mentor.backend.domain.constant.ChannelType;
import com.hcmus.mentor.backend.domain.constant.DomainType;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.GroupUserRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.AuditRecordService;
import com.hcmus.mentor.backend.service.MailService;
import com.hcmus.mentor.backend.service.PermissionService;
import com.hcmus.mentor.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AddMemberToGroupCommandHandler implements Command.Handler<AddMemberToGroupCommand, GroupDetailDto> {

    private final Logger logger = LoggerFactory.getLogger(AddMemberToGroupCommandHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final PermissionService permissionService;
    private final GroupRepository groupRepository;
    private final GroupUserRepository groupUserRepository;
    private final UserService userService;
    private final ChannelRepository channelRepository;
    private final MailService mailService;
    private final AuditRecordService auditRecordService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public GroupDetailDto handle(AddMemberToGroupCommand command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        if (!permissionService.isAdmin(currentUserId, 0)) {
            throw new ForbiddenException("Không có quyền thêm thành viên vào nhóm");
        }

        var group = groupRepository.findById(command.getGroupId()).orElseThrow(() -> new DomainException("Không tìm thấy nhóm"));
        // Filter all emails that not members in group.
        var users = command.getEmails().stream()
                .filter(email -> !email.isEmpty())
                .map(email -> userService.getOrCreateUserByEmail(email, group.getName()))
                .filter(user -> group.getGroupUsers().stream().noneMatch(gu -> gu.getUser().getId().equals(user.getId())))
                .toList();

        users.forEach(user -> {
            if (!user.isStatus()) {
                throw new DomainException(String.format("Tài khoản %s đã bị khoá, xin dùng tài khoản khác", user.getEmail()));
            }
        });

        // The user is not exists in group
        if (!users.isEmpty()) {
            List<GroupUser> groupUsers = users.stream()
                    .map(user -> GroupUser.builder().user(user).group(group).isMentor(command.isMentor()).build())
                    .toList();
            groupUserRepository.saveAll(groupUsers);

            var newMembers = new StringBuilder();
            users.forEach(user -> newMembers.append("\n").append(user.getEmail()));

            auditRecordService.save(AuditRecord.builder()
                    .action(ActionType.UPDATED)
                    .domain(DomainType.GROUP)
                    .entityId(group.getId())
                    .detail(String.format("Thêm %s vào nhóm %s: %s", command.isMentor() ? "mentor" : "mentee", group.getName(), newMembers))
                    .user(userRepository.findById(currentUserId).orElse(null))
                    .build()
            );

            group.getChannels().stream().filter(c -> c.getType() == ChannelType.PUBLIC).forEach(c -> {
                c.getUsers().addAll(users);
                channelRepository.save(c);
            });

            for (String email : command.getEmails()) {
                mailService.sendInvitationToGroupMail(email, group);
            }
        }

        var refreshGroup = groupRepository.findById(command.getGroupId()).orElseThrow(() -> new DomainException("Không tìm thấy nhóm"));

        return modelMapper.map(refreshGroup, GroupDetailDto.class);
    }
}