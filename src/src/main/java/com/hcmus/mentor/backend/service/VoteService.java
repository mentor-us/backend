package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.domain.Choice;
import com.hcmus.mentor.backend.domain.Vote;
import com.hcmus.mentor.backend.controller.payload.request.CreateVoteRequest;
import com.hcmus.mentor.backend.controller.payload.request.DoVotingRequest;
import com.hcmus.mentor.backend.controller.payload.request.UpdateVoteRequest;
import com.hcmus.mentor.backend.controller.payload.response.votes.VoteDetailResponse;
import com.hcmus.mentor.backend.security.UserPrincipal;

import java.util.List;

public interface VoteService {
    VoteDetailResponse get(String userId, String voteId);

    List<VoteDetailResponse> getGroupVotes(String userId, String groupId);

    VoteDetailResponse fulfillChoices(Vote vote);

    VoteDetailResponse.ChoiceDetail fulfillChoice(Choice choice);

    Vote createNewVote(String userId, CreateVoteRequest request);

    boolean updateVote(UserPrincipal user, String voteId, UpdateVoteRequest request);

    boolean deleteVote(UserPrincipal user, String voteId);

    Vote doVoting(DoVotingRequest request);

    VoteDetailResponse.ChoiceDetail getChoiceDetail(
            UserPrincipal user, String voteId, String choiceId);

    List<VoteDetailResponse.ChoiceDetail> getChoiceResults(UserPrincipal user, String voteId);

    boolean closeVote(UserPrincipal user, String voteId);

    boolean reopenVote(UserPrincipal user, String voteId);
}
