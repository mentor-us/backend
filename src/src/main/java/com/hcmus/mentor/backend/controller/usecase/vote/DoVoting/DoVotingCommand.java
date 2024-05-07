package com.hcmus.mentor.backend.controller.usecase.vote.DoVoting;

import an.awesome.pipelinr.Command;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command to do voting.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoVotingCommand implements Command<Void> {
    private String voteId;
    private String voterId;
    private String choiceIds;
}
