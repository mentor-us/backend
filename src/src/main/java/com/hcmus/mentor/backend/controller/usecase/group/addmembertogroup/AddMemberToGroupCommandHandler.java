package com.hcmus.mentor.backend.controller.usecase.group.addmembertogroup;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
import com.hcmus.mentor.backend.controller.usecase.group.common.GroupDetailDto;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.GroupUser;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.GroupUserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.MailService;
import com.hcmus.mentor.backend.service.PermissionService;
import com.hcmus.mentor.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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

    @Override
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

        // The user is not exists in group
        if (!users.isEmpty()) {
            List<GroupUser> groupUsers = users.stream()
                    .map(user -> GroupUser.builder().user(user).group(group).isMentor(command.isMentor()).build())
                    .toList();
            groupUserRepository.saveAll(groupUsers);

            Channel channel = channelRepository.findById(group.getDefaultChannel().getId()).orElse(null);
            if (channel == null) {
                throw new DomainException("Không tìm thấy kênh mặc định của nhóm");
            }

            channel.getUsers().addAll(users);
            channelRepository.save(channel);

            for (String email : command.getEmails()) {
                mailService.sendInvitationToGroupMail(email, group);
            }
        }

        var refreshGroup = groupRepository.findById(command.getGroupId()).orElseThrow(() -> new DomainException("Không tìm thấy nhóm"));

        return modelMapper.map(refreshGroup, GroupDetailDto.class);
    }
}
