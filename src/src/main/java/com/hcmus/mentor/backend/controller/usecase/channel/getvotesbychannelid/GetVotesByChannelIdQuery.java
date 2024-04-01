package com.hcmus.mentor.backend.controller.usecase.channel.getvotesbychannelid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.payload.response.votes.VoteDetailResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class GetVotesByChannelIdQuery implements Command<List<VoteDetailResponse>> {

    /**
     * The ID of the channel to retrieve.
     */
    private String id;
}
