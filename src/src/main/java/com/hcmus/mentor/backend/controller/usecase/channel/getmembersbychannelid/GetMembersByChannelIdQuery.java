package com.hcmus.mentor.backend.controller.usecase.channel.getmembersbychannelid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents a query to retrieve members by their ID.
 */
@Getter
@Setter
@Builder
public class GetMembersByChannelIdQuery implements Command<List<ShortProfile>> {

    /**
     * The ID of the members to retrieve.
     */
    private String id;
}
