package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.domain.dto.ChoiceDto;
import com.hcmus.mentor.backend.domain.Vote;
import com.hcmus.mentor.backend.controller.payload.request.CreateVoteRequest;
import com.hcmus.mentor.backend.controller.payload.request.DoVotingRequest;
import com.hcmus.mentor.backend.controller.payload.request.UpdateVoteRequest;
import com.hcmus.mentor.backend.controller.payload.response.votes.VoteDetailResponse;
import com.hcmus.mentor.backend.security.principal.userdetails.CustomerUserDetails;

import java.util.List;

public interface VoteService {
    VoteDetailResponse get(String userId, String voteId);

    List<VoteDetailResponse> getGroupVotes(String userId, String groupId);

    VoteDetailResponse fulfillChoices(Vote vote);

    VoteDetailResponse.ChoiceDetail fulfillChoice(ChoiceDto choice);

    Vote createNewVote(String userId, CreateVoteRequest request);

    boolean updateVote(CustomerUserDetails user, String voteId, UpdateVoteRequest request);

    boolean deleteVote(CustomerUserDetails user, String voteId);

    Vote doVoting(DoVotingRequest request);

    VoteDetailResponse.ChoiceDetail getChoiceDetail(
            CustomerUserDetails user, String voteId, String choiceId);

    List<VoteDetailResponse.ChoiceDetail> getChoiceResults(CustomerUserDetails user, String voteId);

    boolean closeVote(CustomerUserDetails user, String voteId);

    boolean reopenVote(CustomerUserDetails user, String voteId);
}
