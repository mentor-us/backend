package com.hcmus.mentor.backend.controller.usecase.channel.removechannel;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Voidy;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.GroupRepository;
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
    private final GroupRepository groupRepository;
    private final MessageRepository messageRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public Voidy handle(RemoveChannelCommand command) {
        var userId = loggedUserAccessor.getCurrentUserId();

        var channel = channelRepository.findById(command.getId()).orElseThrow(() -> new DomainException("Không tìm thấy kênh"));
        var group = groupRepository.findById(channel.getParentId()).orElseThrow(() -> new DomainException("Không tìm thấy nhóm"));

        if (!group.getMentors().contains(userId)) {
            throw new ForbiddenException("Không có quyền xoá kênh");
        }

        if (group.getDefaultChannelId().equals(channel.getId())) {
            throw new DomainException("Không thể xoá kênh mặc định");
        }

        group.getChannelIds().remove(channel.getId());
        group.getPrivateIds().remove(channel.getId());

        groupRepository.save(group);

        channelRepository.delete(channel);
        messageRepository.deleteByGroupId(channel.getId());
        return null;
    }
}
