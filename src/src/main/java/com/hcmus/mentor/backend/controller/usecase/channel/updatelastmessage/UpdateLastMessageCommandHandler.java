package com.hcmus.mentor.backend.controller.usecase.channel.updatelastmessage;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Handler for {@link UpdateLastMessageCommand}.
 */
@Component
@RequiredArgsConstructor
public class UpdateLastMessageCommandHandler implements Command.Handler<UpdateLastMessageCommand, Channel>{

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
        channel.setLastMessageId(command.getMessageId());
        channelRepository.save(channel);

        var group = groupRepository.findById(channel.getParentId()).orElse(null);
        if (group == null) {
            return null;
        }

        if(Objects.equals(group.getDefaultChannelId(), command.getChannelId())) {
            group.setLastMessageId(command.getMessageId());
            groupRepository.save(group);
        }
        return channel;
    }
}
