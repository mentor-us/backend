package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.controller.payload.request.CreateVoteRequest;
import com.hcmus.mentor.backend.controller.payload.request.DoVotingRequest;
import com.hcmus.mentor.backend.controller.payload.request.UpdateVoteRequest;
import com.hcmus.mentor.backend.controller.payload.response.votes.VoteDetailResponse;
import com.hcmus.mentor.backend.controller.usecase.vote.common.VoteResult;
import com.hcmus.mentor.backend.domain.Choice;
import com.hcmus.mentor.backend.domain.Vote;
import com.hcmus.mentor.backend.security.principal.userdetails.CustomerUserDetails;

import java.util.List;

public interface VoteService {
    VoteDetailResponse get(String userId, String voteId);

    List<VoteDetailResponse> getGroupVotes(String userId, String channelId);

    VoteDetailResponse fulfillChoices(Vote vote);

    VoteDetailResponse.ChoiceDetail fulfillChoice(Choice choice);

    VoteResult createNewVote(String userId, CreateVoteRequest request);

    boolean updateVote(CustomerUserDetails userDetails, String voteId, UpdateVoteRequest request);

    boolean deleteVote(CustomerUserDetails user, String voteId);

    Vote doVoting(DoVotingRequest request, String userId);

    VoteDetailResponse.ChoiceDetail getChoiceDetail(
            CustomerUserDetails user, String voteId, String choiceId);

    List<VoteDetailResponse.ChoiceDetail> getChoiceResults(CustomerUserDetails user, String voteId);

    void closeVote(CustomerUserDetails user, String voteId);

    void reopenVote(CustomerUserDetails user, String voteId);

}