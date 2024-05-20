package com.hcmus.mentor.backend.controller.usecase.channel.updatelastmessage;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.MessageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handler for {@link UpdateLastMessageCommand}.
 */
@Component
@RequiredArgsConstructor
public class UpdateLastMessageCommandHandler implements Command.Handler<UpdateLastMessageCommand, Channel> {

    private final ChannelRepository channelRepository;
    private final GroupRepository groupRepository;
    private final MessageRepository messageRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Channel handle(final UpdateLastMessageCommand command) {
        var channel = command.getChannel();
        var message = command.getMessage();
        if (channel == null || message == null) {
            return null;
        }
        channel.setLastMessage(message);
        channelRepository.save(channel);

        var group = groupRepository.findById(channel.getGroup().getId()).orElse(null);
        if (group == null) {
            return channel;
        }

        group.setLastMessage(message);
        groupRepository.save(group);

        return channel;
    }
}