package com.hcmus.mentor.backend.controller.usecase.channel.updatechannel;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.constant.ChannelType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents a command to update a channel.
 */
@Getter
@Setter
@Builder
public class UpdateChannelCommand implements Command<Channel> {

    /**
     * The ID of the channel to update.
     */
    private String id;

    /**
     * The new name of the channel.
     */
    private String channelName;

    /**
     * The new description of the channel.
     */
    private String description;

    /**
     * The new type of the channel.
     */
    private ChannelType type;

    /**
     * The new list of user IDs associated with the channel.
     */
    private List<String> userIds;
}
