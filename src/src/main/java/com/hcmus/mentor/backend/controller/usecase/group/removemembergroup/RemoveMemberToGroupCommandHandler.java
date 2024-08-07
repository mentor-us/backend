package com.hcmus.mentor.backend.controller.usecase.group.removemembergroup;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
import com.hcmus.mentor.backend.controller.usecase.group.common.GroupDetailDto;
import com.hcmus.mentor.backend.domain.AuditRecord;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.ActionType;
import com.hcmus.mentor.backend.domain.constant.DomainType;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.GroupUserRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.AuditRecordService;
import com.hcmus.mentor.backend.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RemoveMemberToGroupCommandHandler implements Command.Handler<RemoveMemberToGroupCommand, GroupDetailDto> {

    private final Logger logger = LoggerFactory.getLogger(RemoveMemberToGroupCommandHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final PermissionService permissionService;
    private final GroupRepository groupRepository;
    private final GroupUserRepository groupUserRepository;
    private final ChannelRepository channelRepository;
    private final AuditRecordService auditRecordService;
    private final UserRepository userRepository;

    @Override
    public GroupDetailDto handle(RemoveMemberToGroupCommand command) {
        var curerntUserId = loggedUserAccessor.getCurrentUserId();

        if (!permissionService.isAdmin(curerntUserId, 0)) {
            throw new ForbiddenException("Không có quyền xoá thành viên vào nhóm");
        }

        var group = groupRepository.findById(command.getGroupId()).orElseThrow(() -> new DomainException("Không tìm thấy nhóm"));
        var groupUser = groupUserRepository.findByUserIdAndGroupId(command.getUserId(), group.getId()).orElseThrow(() -> new DomainException("Người dùng không thuộc nhóm"));

        groupUserRepository.delete(groupUser);

        auditRecordService.save(AuditRecord.builder()
                .action(ActionType.UPDATED)
                .domain(DomainType.GROUP)
                .user(userRepository.findById(curerntUserId).orElse(null))
                .entityId(group.getId())
                .detail(String.format("Xoá thành viên %s khỏi nhóm %s", Optional.of(userRepository.findById(command.getUserId()).map(User::getEmail).orElse("")), group.getName()))
                .build());

        var channels = channelRepository.findByGroupId(group.getId());
        for (var channel : channels) {
            var usersInChannel = channel.getUsers();

            usersInChannel.removeIf(u -> Objects.equals(u.getId(), command.getUserId()));
            channel.setUsers(usersInChannel);

            channelRepository.save(channel);
        }

        logger.info("Đã xóa thành viên {} khỏi nhóm {}", command.getUserId(), group.getId());

        var refreshGroup = groupRepository.findById(command.getGroupId()).orElseThrow(() -> new DomainException("Không tìm thấy nhóm"));

        return modelMapper.map(refreshGroup, GroupDetailDto.class);
    }
}