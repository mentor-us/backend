package com.hcmus.mentor.backend.controller;

import com.hcmus.mentor.backend.entity.Channel;
import com.hcmus.mentor.backend.payload.request.groups.AddChannelRequest;
import com.hcmus.mentor.backend.payload.request.groups.UpdateChannelRequest;
import com.hcmus.mentor.backend.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.security.CurrentUser;
import com.hcmus.mentor.backend.security.UserPrincipal;
import com.hcmus.mentor.backend.service.ChannelService;
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

@Tag(name = "Channel APIs", description = "REST APIs for Channel collections")
@RestController
@RequestMapping("/api/channels")
@SecurityRequirement(name = "bearer")
public class ChannelController {

    private final ChannelService channelService;

    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    @Operation(summary = "Add new group channel (Mobile)",
            description = "Add new channel in single group (Channel could be 1 - 1 also)",
            tags = "Channel APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Add successfully",
                    content = @Content(array = @ArraySchema(schema
                            = @Schema(implementation = Channel.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @PostMapping(value = {"", "/"})
    public ResponseEntity<Channel> addChannel(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                              @RequestBody AddChannelRequest request) {
        Channel channel = channelService.addChannel(userPrincipal.getId(), request);
        if (channel == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(channel);
    }

    @Operation(summary = "Remove group channel (Mobile)",
            description = "Remove channel in single group (Channel could be 1 - 1 also)",
            tags = "Channel APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Add successfully",
                    content = @Content(array = @ArraySchema(schema
                            = @Schema(implementation = Void.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> removeChannel(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                              @PathVariable String channelId) {
        boolean isDeleted = channelService.removeChannel(userPrincipal, channelId);
        if (!isDeleted) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get all channels of group (Mobile)",
            description = "Get all channels in single group (Channel could be 1 - 1 also)",
            tags = "Channel APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Add successfully",
                    content = @Content(array = @ArraySchema(schema
                            = @Schema(implementation = Channel.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @GetMapping(value = {"", "/"})
    public ResponseEntity<List<Channel>> getChannels(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                                     @RequestParam(required = false) String parentId) {
        List<Channel> channels = channelService.getChannels(userPrincipal, parentId);
        return ResponseEntity.ok(channels);
    }

    @Operation(summary = "Update group channel (Mobile)",
            description = "Update a channel in single group (Channel could be 1 - 1 also)",
            tags = "Channel APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Add successfully",
                    content = @Content(array = @ArraySchema(schema
                            = @Schema(implementation = Channel.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @PatchMapping("/{channelId}")
    public ResponseEntity<Channel> updateChannel(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                                 @PathVariable String channelId,
                                                 @RequestBody UpdateChannelRequest request) {
        Channel channel = channelService.updateChannel(userPrincipal, channelId, request);
        if (channel == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(channel);
    }

    @Operation(summary = "Get all members of channel (Mobile)",
            description = "Get all members of channel in group (Channel could be 1 - 1 also)",
            tags = "Channel APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Add successfully",
                    content = @Content(array = @ArraySchema(schema
                            = @Schema(implementation = ShortProfile.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @GetMapping("/{channelId}/members")
    public ResponseEntity<List<ShortProfile>> getChannelMembers(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                                 @PathVariable String channelId) {
        List<ShortProfile> members = channelService.getChannelMembers(userPrincipal, channelId);
        return ResponseEntity.ok(members);
    }
}
