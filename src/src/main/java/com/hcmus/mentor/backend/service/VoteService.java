package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.entity.Vote;
import com.hcmus.mentor.backend.payload.request.CreateVoteRequest;
import com.hcmus.mentor.backend.payload.request.DoVotingRequest;
import com.hcmus.mentor.backend.payload.request.UpdateVoteRequest;
import com.hcmus.mentor.backend.payload.response.votes.VoteDetailResponse;
import com.hcmus.mentor.backend.security.UserPrincipal;
import java.util.List;

public interface VoteService {
  VoteDetailResponse get(String userId, String voteId);

  List<VoteDetailResponse> getGroupVotes(String userId, String groupId);

  VoteDetailResponse fulfillChoices(Vote vote);

  VoteDetailResponse.ChoiceDetail fulfillChoice(Vote.Choice choice);

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
