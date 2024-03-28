package com.hcmus.mentor.backend.controller.usecase.channel.updatelastmessage;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.domain.Channel;
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
    private String channelId;

    /**
     * The ID of the group.
     */
    private String messageId;
}
