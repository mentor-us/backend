package vn.edu.hcmus.mentor.backend.controller;

import an.awesome.pipelinr.Pipeline;
import vn.edu.hcmus.mentor.backend.controller.payload.response.ShortMediaMessage;
import vn.edu.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import vn.edu.hcmus.mentor.backend.controller.payload.response.votes.VoteDetailResponse;
import vn.edu.hcmus.mentor.backend.controller.usecase.channel.addchannel.AddChannelCommand;
import vn.edu.hcmus.mentor.backend.controller.usecase.channel.common.ChannelDetailDto;
import vn.edu.hcmus.mentor.backend.controller.usecase.channel.getchannelbyid.GetChannelByIdQuery;
import vn.edu.hcmus.mentor.backend.controller.usecase.channel.getchannelsbygroupid.GetChannelsByGroupIdQuery;
import vn.edu.hcmus.mentor.backend.controller.usecase.channel.getmediabychannelid.GetMediaByChannelIdQuery;
import vn.edu.hcmus.mentor.backend.controller.usecase.channel.getmeetingsbychannelid.GetMeetingsByChannelIdQuery;
import vn.edu.hcmus.mentor.backend.controller.usecase.channel.getmembersbychannelid.GetMembersByChannelIdQuery;
import vn.edu.hcmus.mentor.backend.controller.usecase.channel.gettasksbychannelid.GetTasksByIdQuery;
import vn.edu.hcmus.mentor.backend.controller.usecase.channel.getvotesbychannelid.GetVotesByChannelIdQuery;
import vn.edu.hcmus.mentor.backend.controller.usecase.channel.removechannel.RemoveChannelCommand;
import vn.edu.hcmus.mentor.backend.controller.usecase.channel.updatechannel.UpdateChannelCommand;
import vn.edu.hcmus.mentor.backend.controller.usecase.meeting.common.MeetingResult;
import vn.edu.hcmus.mentor.backend.controller.usecase.task.common.TaskDetailResult;
import vn.edu.hcmus.mentor.backend.domain.Channel;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Channel controller.
 */
@Tag(name = "channels")
@RestController
@RequestMapping("/api/channels")
@SecurityRequirement(name = "bearer")
@RequiredArgsConstructor
public class ChannelController {

    private final Pipeline pipeline;

    /**
     * Retrieves a list of channels by group ID.
     *
     * @param parentId the parent ID to filter channels
     * @return ResponseEntity containing a list of channels
     */
    @GetMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<List<Channel>> getChannelsByGroupId(
            @RequestParam(required = false) String parentId) {
        var query = GetChannelsByGroupIdQuery.builder()
                .groupId(parentId)
                .build();

        var channels = pipeline.send(query);

        return ResponseEntity.ok(channels);
    }

    /**
     * Retrieves a group detail response by ID.
     *
     * @param id the ID of the group
     * @return ResponseEntity containing the group detail response
     */
    @GetMapping("{id}")
    public ResponseEntity<ChannelDetailDto> getChannelById(
            @PathVariable String id) {
        var query = GetChannelByIdQuery.builder()
                .id(id)
                .build();

        var channel = pipeline.send(query);

        return ResponseEntity.ok(channel);
    }

    /**
     * Retrieves media messages by ID.
     *
     * @param id the ID of the media
     * @return ResponseEntity containing a list of short media messages
     */
    @GetMapping("{id}/media")
    public ResponseEntity<List<ShortMediaMessage>> getMediaById(
            @PathVariable String id) {
        var quey = GetMediaByChannelIdQuery.builder()
                .id(id)
                .build();

        return ResponseEntity.ok(pipeline.send(quey));
    }

    /**
     * Retrieves members by ID.
     *
     * @param id the ID of the member
     * @return ResponseEntity containing a list of short profiles
     */
    @GetMapping("{id}/members")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<List<ShortProfile>> getMembersById(
            @PathVariable String id) {
        var query = GetMembersByChannelIdQuery.builder()
                .id(id)
                .build();

        return ResponseEntity.ok(pipeline.send(query));
    }

    @GetMapping("{id}/meetings")
    public ResponseEntity<List<MeetingResult>> getMeetingsById(
            @PathVariable String id) {
        var query = GetMeetingsByChannelIdQuery.builder()
                .id(id)
                .build();

        return ResponseEntity.ok(pipeline.send(query));
    }

    @GetMapping("{id}/votes")
    public ResponseEntity<List<VoteDetailResponse>> getVotesById(
            @PathVariable String id) {
        var query = GetVotesByChannelIdQuery.builder()
                .id(id)
                .build();

        return ResponseEntity.ok(pipeline.send(query));
    }

    @GetMapping("{id}/tasks")
    public ResponseEntity<List<TaskDetailResult>> getTasksById(
            @PathVariable String id) {
        var query = GetTasksByIdQuery.builder()
                .id(id)
                .build();

        return ResponseEntity.ok(pipeline.send(query));
    }

    /**
     * Adds a new channel.
     *
     * @param request the request to add the channel
     * @return ResponseEntity containing the added channel
     */
    @PostMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Channel> addChannel(@Valid @RequestBody AddChannelCommand request) {
        return ResponseEntity.ok(pipeline.send(request));
    }

    /**
     * Updates a channel by ID.
     *
     * @param id      the ID of the channel to updates
     * @param command the command to update the channel
     * @return ResponseEntity containing the updated channel
     */
    @PatchMapping("{id}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Channel> updateChannel(
            @PathVariable String id,
            @RequestBody UpdateChannelCommand command) {
        command.setId(id);

        return ResponseEntity.ok(pipeline.send(command));
    }

    /**
     * Removes a channel by ID.
     *
     * @param id the ID of the channel to remove
     * @return ResponseEntity indicating the success of the removal operation
     */
    @DeleteMapping("{id}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<Void> removeChannel(
            @PathVariable String id) {
        var command = RemoveChannelCommand.builder()
                .id(id)
                .build();

        pipeline.send(command);

        return ResponseEntity.ok().build();
    }
}
