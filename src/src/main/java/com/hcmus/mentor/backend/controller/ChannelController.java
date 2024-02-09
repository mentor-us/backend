package com.hcmus.mentor.backend.controller;

import com.hcmus.mentor.backend.controller.payload.request.groups.AddChannelRequest;
import com.hcmus.mentor.backend.controller.payload.request.groups.UpdateChannelRequest;
import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.security.CurrentUser;
import com.hcmus.mentor.backend.security.UserPrincipal;
import com.hcmus.mentor.backend.service.ChannelService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    private final ChannelService channelService;

    /**
     * Adds a new channel to a group (Mobile).
     *
     * @param userPrincipal The current user's principal information.
     * @param request       The request containing information to create a new channel.
     * @return ResponseEntity containing the newly created channel.
     */
    @PostMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Channel> addChannel(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @RequestBody AddChannelRequest request) {
        Channel channel = channelService.addChannel(userPrincipal.getId(), request);
        if (channel == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(channel);
    }

    /**
     * Removes a channel from a group (Mobile).
     *
     * @param userPrincipal The current user's principal information.
     * @param channelId     The ID of the channel to be removed.
     * @return ResponseEntity indicating the success of the removal operation.
     */
    @DeleteMapping("{channelId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Void> removeChannel(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable String channelId) {
        boolean isDeleted = channelService.removeChannel(userPrincipal, channelId);
        if (!isDeleted) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Gets all channels of a group (Mobile).
     *
     * @param userPrincipal The current user's principal information.
     * @param parentId      The ID of the parent channel (optional).
     * @return ResponseEntity containing a list of channels in the group.
     */
    @GetMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<List<Channel>> getChannels(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @RequestParam(required = false) String parentId) {
        List<Channel> channels = channelService.getChannels(userPrincipal, parentId);
        return ResponseEntity.ok(channels);
    }

    /**
     * Updates a group channel (Mobile).
     *
     * @param userPrincipal The current user's principal information.
     * @param channelId     The ID of the channel to be updated.
     * @param request       The request containing updated information for the channel.
     * @return ResponseEntity containing the updated channel.
     */
    @PatchMapping("{channelId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Channel> updateChannel(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable String channelId,
            @RequestBody UpdateChannelRequest request) {
        Channel channel = channelService.updateChannel(userPrincipal, channelId, request);
        if (channel == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(channel);
    }

    /**
     * Gets all members of a channel (Mobile).
     *
     * @param userPrincipal The current user's principal information.
     * @param channelId     The ID of the channel to get members from.
     * @return ResponseEntity containing a list of short profiles of channel members.
     */
    @GetMapping("{channelId}/members")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<List<ShortProfile>> getChannelMembers(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable String channelId) {
        List<ShortProfile> members = channelService.getChannelMembers(userPrincipal, channelId);
        return ResponseEntity.ok(members);
    }
}
