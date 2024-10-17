package vn.edu.hcmus.mentor.backend.controller.usecase.channel.updatelastmessage;

import an.awesome.pipelinr.Command;
import vn.edu.hcmus.mentor.backend.domain.Channel;
import vn.edu.hcmus.mentor.backend.repository.ChannelRepository;
import vn.edu.hcmus.mentor.backend.repository.GroupRepository;
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

        channel.ping();
        channel.setLastMessage(message);
        channelRepository.save(channel);

        var group = channel.getGroup();
        if (group == null) {
            return channel;
        }

        group.ping();
        group.setLastMessage(message);
        groupRepository.save(group);

        return channel;
    }
}