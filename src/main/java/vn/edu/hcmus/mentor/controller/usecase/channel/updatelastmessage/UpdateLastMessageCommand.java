package vn.edu.hcmus.mentor.controller.usecase.channel.updatelastmessage;

import an.awesome.pipelinr.Command;
import vn.edu.hcmus.mentor.domain.Channel;
import vn.edu.hcmus.mentor.domain.Message;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Command for updating the last message of a channel and group.
 */
@Getter
@Setter
@Builder
public class UpdateLastMessageCommand implements Command<Channel> {

    /**
     * The ID of the channel.
     */
    private Channel channel;

    /**
     * The ID of the group.
     */
    private Message message;
}