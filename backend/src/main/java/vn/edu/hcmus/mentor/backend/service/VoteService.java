package vn.edu.hcmus.mentor.backend.service;

import vn.edu.hcmus.mentor.backend.controller.payload.request.votes.CreateVoteRequest;
import vn.edu.hcmus.mentor.backend.controller.payload.request.votes.DoVotingRequest;
import vn.edu.hcmus.mentor.backend.controller.payload.request.votes.UpdateVoteRequest;
import vn.edu.hcmus.mentor.backend.controller.payload.response.votes.VoteDetailResponse;
import vn.edu.hcmus.mentor.backend.controller.usecase.vote.common.VoteResult;
import vn.edu.hcmus.mentor.backend.domain.Choice;
import vn.edu.hcmus.mentor.backend.domain.Vote;
import vn.edu.hcmus.mentor.backend.security.principal.userdetails.CustomerUserDetails;

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