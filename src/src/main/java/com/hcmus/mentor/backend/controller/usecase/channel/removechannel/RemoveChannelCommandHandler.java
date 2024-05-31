package com.hcmus.mentor.backend.controller.usecase.channel.removechannel;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Voidy;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
import com.hcmus.mentor.backend.domain.Message;
import com.hcmus.mentor.backend.domain.constant.ChannelStatus;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.MessageRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for {@link RemoveChannelCommand}.
 */
@Component
@RequiredArgsConstructor
public class RemoveChannelCommandHandler implements Command.Handler<RemoveChannelCommand, Voidy> {

    private final LoggedUserAccessor loggedUserAccessor;
    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;
    private final PermissionService permissionService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Voidy handle(RemoveChannelCommand command) {
        var userId = loggedUserAccessor.getCurrentUserId();

        var channel = channelRepository.findById(command.getId()).orElseThrow(() -> new DomainException("Không tìm thấy kênh"));
        if (channel.getStatus().equals(ChannelStatus.DELETED)) {
            throw new DomainException("Kênh đã bị xoá");
        }
        var group = channel.getGroup();

        if (!permissionService.isMentorInChannel(command.getId(), userId)) {
            throw new ForbiddenException("Không có quyền xoá kênh");
        }

        if (group.getDefaultChannel().getId().equals(channel.getId())) {
            throw new DomainException("Không thể xoá kênh mặc định");
        }

        channel.setStatus(ChannelStatus.DELETED);
        channelRepository.save(channel);
        messageRepository.deleteAllByChannelId(channel.getId(), Message.Status.DELETED);
        return null;
    }
}