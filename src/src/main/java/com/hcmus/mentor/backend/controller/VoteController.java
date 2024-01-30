package com.hcmus.mentor.backend.controller;

import com.corundumstudio.socketio.SocketIOServer;
import com.hcmus.mentor.backend.domain.Vote;
import com.hcmus.mentor.backend.controller.payload.APIResponse;
import com.hcmus.mentor.backend.controller.payload.request.CreateVoteRequest;
import com.hcmus.mentor.backend.controller.payload.request.DoVotingRequest;
import com.hcmus.mentor.backend.controller.payload.request.UpdateVoteRequest;
import com.hcmus.mentor.backend.controller.payload.response.votes.VoteDetailResponse;
import com.hcmus.mentor.backend.repository.VoteRepository;
import com.hcmus.mentor.backend.service.VoteService;
import com.hcmus.mentor.backend.security.CurrentUser;
import com.hcmus.mentor.backend.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Vote APIs", description = "REST APIs for Vote collections")
@RestController
@RequestMapping("/api/votes")
@SecurityRequirement(name = "bearer")
public class VoteController {

    private final VoteRepository voteRepository;

    private final VoteService voteService;

    private final SocketIOServer socketServer;

    public VoteController(
            VoteRepository voteRepository, VoteService voteService, SocketIOServer socketServer) {
        this.voteRepository = voteRepository;
        this.voteService = voteService;
        this.socketServer = socketServer;
    }

    @Operation(
            summary = "Get all votes on group",
            description = "Get list of votes in group",
            tags = "Vote APIs")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Get successfully",
                    content =
                    @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Need authentication"),
    })
    @GetMapping(value = {""})
    public ResponseEntity<List<VoteDetailResponse>> all(
            @Parameter(hidden = true) @CurrentUser UserPrincipal user, @RequestParam String groupId) {
        List<VoteDetailResponse> votes = voteService.getGroupVotes(user.getId(), groupId);
        if (votes == null || votes.size() == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(votes);
    }

    @Operation(
            summary = "Get vote detail",
            description = "Get detail information of vote",
            tags = "Vote APIs")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Get successfully",
                    content =
                    @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Need authentication"),
    })
    @GetMapping("/{voteId}")
    public ResponseEntity<VoteDetailResponse> get(
            @Parameter(hidden = true) @CurrentUser UserPrincipal user, @PathVariable String voteId) {
        VoteDetailResponse vote = voteService.get(user.getId(), voteId);
        if (vote == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(vote);
    }

    @Operation(
            summary = "Create new vote",
            description = "Create new vote in group",
            tags = "Vote APIs")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Create successfully",
                    content =
                    @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Need authentication"),
    })
    @PostMapping(value = {""})
    public ResponseEntity<Vote> create(
            @Parameter(hidden = true) @CurrentUser UserPrincipal user,
            @RequestBody CreateVoteRequest request) {
        Vote vote = voteService.createNewVote(user.getId(), request);
        if (vote == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(vote);
    }

    @Operation(
            summary = "Update vote",
            description = "Update information of vote",
            tags = "Vote APIs")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Update successfully",
                    content =
                    @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Need authentication"),
    })
    @PatchMapping("/{voteId}")
    public ResponseEntity<Void> update(
            @Parameter(hidden = true) @CurrentUser UserPrincipal user,
            @PathVariable String voteId,
            @RequestBody UpdateVoteRequest request) {
        boolean isUpdated = voteService.updateVote(user, voteId, request);
        if (!isUpdated) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Delete vote",
            description = "Delete single vote in group",
            tags = "Vote APIs")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Delete successfully",
                    content =
                    @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Need authentication"),
    })
    @DeleteMapping("/{voteId}")
    public ResponseEntity<Void> delete(
            @Parameter(hidden = true) @CurrentUser UserPrincipal user, @PathVariable String voteId) {
        boolean isDeleted = voteService.deleteVote(user, voteId);
        if (!isDeleted) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Get choices information",
            description = "Get all information with voters of a choice in Vote",
            tags = "Vote APIs")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Get successfully",
                    content =
                    @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Need authentication"),
    })
    @GetMapping("/{voteId}/choices/{choiceId}")
    public ResponseEntity<VoteDetailResponse.ChoiceDetail> getChoiceDetail(
            @Parameter(hidden = true) @CurrentUser UserPrincipal user,
            @PathVariable String voteId,
            @PathVariable String choiceId) {
        VoteDetailResponse.ChoiceDetail choiceDetail =
                voteService.getChoiceDetail(user, voteId, choiceId);
        if (choiceDetail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(choiceDetail);
    }

    @Operation(
            summary = "Get vote result",
            description = "Get final result of vote when timeEnd's coming",
            tags = "Vote APIs")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Get successfully",
                    content =
                    @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Need authentication"),
    })
    @GetMapping("/{voteId}/result")
    public ResponseEntity<List<VoteDetailResponse.ChoiceDetail>> getChoiceResult(
            @Parameter(hidden = true) @CurrentUser UserPrincipal user, @PathVariable String voteId) {
        List<VoteDetailResponse.ChoiceDetail> choiceResult = voteService.getChoiceResults(user, voteId);
        if (choiceResult == null || choiceResult.size() == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(choiceResult);
    }

    @Operation(
            summary = "Close vote",
            description = "Creator or Admin can close vote before timeEnd",
            tags = "Vote APIs")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Close successfully",
                    content =
                    @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Need authentication"),
    })
    @PatchMapping("/{voteId}/close")
    public ResponseEntity<Void> closeVote(
            @Parameter(hidden = true) @CurrentUser UserPrincipal user, @PathVariable String voteId) {
        boolean isSuccess = voteService.closeVote(user, voteId);
        if (!isSuccess) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Reopen vote",
            description = "Creator or Admin can reopen vote after ending",
            tags = "Vote APIs")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reopen successfully",
                    content =
                    @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Need authentication"),
    })
    @PatchMapping("/{voteId}/reopen")
    public ResponseEntity<Void> reopenVote(
            @Parameter(hidden = true) @CurrentUser UserPrincipal user, @PathVariable String voteId) {
        boolean isSuccess = voteService.reopenVote(user, voteId);
        if (!isSuccess) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Doing vote",
            description = "Group members can do vote on group vote",
            tags = "Vote APIs")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Vote successfully",
                    content =
                    @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Need authentication"),
    })
    @PostMapping("/{voteId}/voting")
    public ResponseEntity<Void> doVoting(
            @Parameter(hidden = true) @CurrentUser UserPrincipal user,
            @PathVariable String voteId,
            @RequestBody DoVotingRequest request) {
        Vote vote = voteService.doVoting(request);
        if (vote == null) {
            return ResponseEntity.badRequest().build();
        }

        socketServer.getRoomOperations(vote.getGroupId()).sendEvent("receive_voting", vote);

        return ResponseEntity.ok().build();
    }
}
