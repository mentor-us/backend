package com.hcmus.mentor.backend.controller.usecase.channel.removechannel;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Voidy;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a command to remove a channel.
 */
@Getter
@Setter
@Builder
public class RemoveChannelCommand implements Command<Voidy> {

    /**
     * The ID of the channel to remove.
     */
    private String id;
}
