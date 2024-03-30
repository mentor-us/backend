package com.hcmus.mentor.backend.controller.usecase.channel.getchannelsbygroupid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.domain.Channel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents a query to retrieve channels by group ID.
 */
@Getter
@Setter
@Builder
public class GetChannelsByGroupIdQuery implements Command<List<Channel>> {

    /**
     * The ID of the group to retrieve channels for.
     */
    private String groupId;
}
