package com.hcmus.mentor.backend.controller;

import an.awesome.pipelinr.Pipeline;
import com.corundumstudio.socketio.SocketIOServer;
import com.hcmus.mentor.backend.controller.payload.request.DoVotingRequest;
import com.hcmus.mentor.backend.controller.payload.request.UpdateVoteRequest;
import com.hcmus.mentor.backend.controller.payload.response.votes.VoteDetailResponse;
import com.hcmus.mentor.backend.controller.usecase.vote.common.VoteResult;
import com.hcmus.mentor.backend.controller.usecase.vote.createvote.CreateVoteCommand;
import com.hcmus.mentor.backend.domain.Vote;
import com.hcmus.mentor.backend.security.principal.CurrentUser;
import com.hcmus.mentor.backend.security.principal.userdetails.CustomerUserDetails;
import com.hcmus.mentor.backend.service.MessageService;
import com.hcmus.mentor.backend.service.VoteService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Vote controller.
 */
@Tag(name = "votes")
@RestController
@RequestMapping("api/votes")
@SecurityRequirement(name = "bearer")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;
    private final SocketIOServer socketServer;
    private final MessageService messageService;
    private final Pipeline pipeline;

    /**
     * (Use api /api/channels/votes) Get all votes on group.
     *
     * @param user    Current authenticated user's principal.
     * @param groupId Group ID to get votes from.
     * @return ResponseEntity<List < VoteDetailResponse>> - Response containing a list of votes in the group.
     */
    @Deprecated(forRemoval = true)
    @GetMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<List<VoteDetailResponse>> all(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails user, @RequestParam String groupId) {
        List<VoteDetailResponse> votes = voteService.getGroupVotes(user.getId(), groupId);
        if (votes == null || votes.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(votes);
    }

    /**
     * Get vote detail.
     *
     * @param user   Current authenticated user's principal.
     * @param voteId Vote ID to get details.
     * @return ResponseEntity<VoteDetailResponse> - Response containing detailed information of the vote.
     */
    @GetMapping("{voteId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<VoteDetailResponse> get(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails user, @PathVariable String voteId) {
        VoteDetailResponse vote = voteService.get(user.getId(), voteId);
        if (vote == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(vote);
    }


    /**
     * Create vote.
     *
     * @param command CreateVoteCommand containing details for creating the vote.
     * @return ResponseEntity<Vote> - Response containing the created vote.
     */
    @PostMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<VoteResult> create(
            @RequestBody CreateVoteCommand command) {
        var vote = pipeline.send(command);

        return ResponseEntity.ok(vote);
    }

    /**
     * Update vote.
     *
     * @param user    Current authenticated user's principal.
     * @param voteId  Vote ID to update.
     * @param request UpdateVoteRequest containing details for updating the vote.
     * @return ResponseEntity<Void> - Response entity indicating success or failure of the update operation.
     */
    @PatchMapping("{voteId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Void> update(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails user,
            @PathVariable String voteId,
            @RequestBody UpdateVoteRequest request) {
        boolean isUpdated = voteService.updateVote(user, voteId, request);
        if (!isUpdated) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Delete vote.
     *
     * @param user   Current authenticated user's principal.
     * @param voteId Vote ID to delete.
     * @return ResponseEntity<Void> - Response entity indicating success or failure of the deletion operation.
     */
    @DeleteMapping("{voteId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Void> delete(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails user, @PathVariable String voteId) {
        boolean isDeleted = voteService.deleteVote(user, voteId);
        if (!isDeleted) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Get choices information.
     *
     * @param user     Current authenticated user's principal.
     * @param voteId   Vote ID for which choices information is requested.
     * @param choiceId Choice ID to get details.
     * @return ResponseEntity<VoteDetailResponse.ChoiceDetail> - Response containing detailed information of the choice in the vote.
     */
    @GetMapping("{voteId}/choices/{choiceId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<VoteDetailResponse.ChoiceDetail> getChoiceDetail(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails user,
            @PathVariable String voteId,
            @PathVariable String choiceId) {

        VoteDetailResponse.ChoiceDetail choiceDetail =
                voteService.getChoiceDetail(user, voteId, choiceId);
        if (choiceDetail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(choiceDetail);
    }

    /**
     * Get vote result.
     *
     * @param user   Current authenticated user's principal.
     * @param voteId Vote ID to get results.
     * @return ResponseEntity<List < VoteDetailResponse.ChoiceDetail>> - Response containing the results of the vote.
     */
    @GetMapping("{voteId}/result")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<List<VoteDetailResponse.ChoiceDetail>> getChoiceResult(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails user,
            @PathVariable String voteId) {

        List<VoteDetailResponse.ChoiceDetail> choiceResult = voteService.getChoiceResults(user, voteId);
        return ResponseEntity.ok(choiceResult);
    }

    /**
     * Close vote.
     *
     * @param user   Current authenticated user's principal.
     * @param voteId Vote ID to close.
     * @return ResponseEntity<Void> - Response entity indicating success or failure of the close vote operation.
     */
    @PatchMapping("{voteId}/close")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<Void> closeVote(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails user,
            @PathVariable String voteId) {

        voteService.closeVote(user, voteId);
        return ResponseEntity.ok().build();
    }

    /**
     * Reopen vote.
     *
     * @param user   Current authenticated user's principal.
     * @param voteId Vote ID to reopen.
     * @return ResponseEntity<Void> - Response entity indicating success or failure of the reopen vote operation.
     */
    @PatchMapping("{voteId}/reopen")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<Void> reopenVote(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails user,
            @PathVariable String voteId) {

        voteService.reopenVote(user, voteId);
        return ResponseEntity.ok().build();
    }

    /**
     * Doing vote.
     *
     * @param user    Current authenticated user's principal.
     * @param voteId  Vote ID on which voting is done.
     * @param request DoVotingRequest containing the voting details.
     * @return ResponseEntity<Void> - Response entity indicating success or failure of the voting operation.
     */
    @PostMapping("{voteId}/voting")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<Void> doVoting(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails user,
            @PathVariable String voteId,
            @RequestBody DoVotingRequest request) {

        Vote vote = voteService.doVoting(request, user.getId());

        messageService.updateCreatedDateVoteMessage(voteId);
        socketServer.getRoomOperations(vote.getGroup().getId()).sendEvent("receive_voting", vote);

        return ResponseEntity.ok().build();
    }
}