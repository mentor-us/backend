package com.hcmus.mentor.backend.controller.usecase.channel.updatelastmessage;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handler for {@link UpdateLastMessageCommand}.
 */
@Component
@RequiredArgsConstructor
public class UpdateLastMessageHandler implements Command.Handler<UpdateLastMessageCommand, Channel>{

    private final ChannelRepository channelRepository;
    private final GroupRepository groupRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public Channel handle(final UpdateLastMessageCommand command) {
        var channel = channelRepository.findById(command.getChannelId()).orElse(null);
        if (channel == null) {
            return null;
        }

        var group = groupRepository.findById(channel.getParentId()).orElse(null);
        if (group == null) {
            return null;
        }

        group.setLastMessageId(command.getMessageId());
        groupRepository.save(group);
        return channel;
    }
}
