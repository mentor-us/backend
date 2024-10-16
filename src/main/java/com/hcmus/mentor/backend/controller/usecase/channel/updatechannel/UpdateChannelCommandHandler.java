package com.hcmus.mentor.backend.controller.usecase.channel.updatechannel;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
import com.hcmus.mentor.backend.domain.AuditRecord;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.constant.ActionType;
import com.hcmus.mentor.backend.domain.constant.DomainType;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.AuditRecordService;
import com.hcmus.mentor.backend.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handler for {@link UpdateChannelCommand}.
 */
@Component
@RequiredArgsConstructor
public class UpdateChannelCommandHandler implements Command.Handler<UpdateChannelCommand, Channel> {

    private final LoggedUserAccessor loggedUserAccessor;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;
    private final AuditRecordService auditRecordService;

    /**
     * {@inheritDoc}
     */
    @Override
    public Channel handle(UpdateChannelCommand request) {
        var userId = loggedUserAccessor.getCurrentUserId();
        if (!permissionService.isMentorInChannel(request.getId(), userId)) {
            throw new ForbiddenException("Không có quyền chỉnh sửa kênh");
        }
        var channel = channelRepository.findById(request.getId()).orElseThrow(() -> new DomainException("Không tìm thấy kênh"));

        var detailUpdate = new StringBuilder();
        if (!channel.getName().equals(request.getChannelName())) {
            channel.setName(request.getChannelName());
            detailUpdate.append("\n").append("Tên kênh: ").append(request.getChannelName());
        }
        if (!channel.getDescription().equals(request.getDescription())) {
            channel.setDescription(request.getDescription());
            detailUpdate.append("\n").append("Mô tả: ").append(request.getDescription());
        }
        if (!channel.getType().equals(request.getType())) {
            channel.setType(request.getType());
            detailUpdate.append("\n").append("Loại kênh: ").append(request.getType());
        }
        if (!channel.getUsers().equals(userRepository.findAllByIdIn(request.getUserIds()))) {
            detailUpdate.append("\n").append("Thành viên: ").append(request.getUserIds());
            channel.setUsers(userRepository.findAllByIdIn(request.getUserIds()));
        }

        if (!detailUpdate.isEmpty()) {
            channelRepository.save(channel);
            auditRecordService.save(AuditRecord.builder()
                    .domain(DomainType.CHANNEL)
                    .action(ActionType.CREATED)
                    .user(userRepository.findById(userId).orElse(null))
                    .entityId(channel.getId())
                    .detail(String.format("Chỉnh sửa kênh: %s", detailUpdate))
                    .build());
        }

        return channel;

    }
}