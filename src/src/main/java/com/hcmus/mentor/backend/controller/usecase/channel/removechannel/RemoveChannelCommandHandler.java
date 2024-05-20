package com.hcmus.mentor.backend.controller.usecase.channel.removechannel;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Voidy;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
import com.hcmus.mentor.backend.domain.Message;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.MessageRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handler for {@link RemoveChannelCommand}.
 */
@Component
@RequiredArgsConstructor
public class RemoveChannelCommandHandler implements Command.Handler<RemoveChannelCommand, Voidy> {

    private final LoggedUserAccessor loggedUserAccessor;
    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public Voidy handle(RemoveChannelCommand command) {
        var userId = loggedUserAccessor.getCurrentUserId();

        var channel = channelRepository.findById(command.getId()).orElseThrow(() -> new DomainException("Không tìm thấy kênh"));
        var group = channel.getGroup();
        if (!group.getMentors().contains(userId)) {
            throw new ForbiddenException("Không có quyền xoá kênh");
        }

        if (group.getDefaultChannel().getId().equals(channel.getId())) {
            throw new DomainException("Không thể xoá kênh mặc định");
        }

        channelRepository.delete(channel);
        messageRepository.deleteAllByChannelId(channel.getId(), Message.Status.DELETED);
        return null;
    }
}