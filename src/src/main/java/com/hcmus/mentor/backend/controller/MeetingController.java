package com.hcmus.mentor.backend.controller;

import com.corundumstudio.socketio.SocketIOServer;
import com.hcmus.mentor.backend.entity.Meeting;
import com.hcmus.mentor.backend.payload.APIResponse;
import com.hcmus.mentor.backend.payload.request.RescheduleMeetingRequest;
import com.hcmus.mentor.backend.payload.request.meetings.CreateMeetingRequest;
import com.hcmus.mentor.backend.payload.request.meetings.UpdateMeetingRequest;
import com.hcmus.mentor.backend.payload.response.meetings.MeetingAttendeeResponse;
import com.hcmus.mentor.backend.payload.response.meetings.MeetingDetailResponse;
import com.hcmus.mentor.backend.payload.response.meetings.MeetingResponse;
import com.hcmus.mentor.backend.repository.MeetingRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.CurrentUser;
import com.hcmus.mentor.backend.security.UserPrincipal;
import com.hcmus.mentor.backend.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Meeting APIs", description = "REST APIs for meeting collections")
@RestController
@RequestMapping("/api/meetings")
@SecurityRequirement(name = "bearer")
public class MeetingController {

  private final MeetingService meetingService;

  private final GroupService groupService;

  private final MeetingRepository meetingRepository;

  private final PermissionService permissionService;

  private final MessageService messageService;

  private final SocketIOServer socketServer;

  private final UserRepository userRepository;

  private final NotificationService notificationService;

  public MeetingController(
      MeetingService meetingService,
      GroupService groupService,
      MeetingRepository meetingRepository,
      PermissionService permissionService,
      MessageService messageService,
      SocketIOServer socketServer,
      UserRepository userRepository,
      NotificationService notificationService) {
    this.meetingService = meetingService;
    this.groupService = groupService;
    this.meetingRepository = meetingRepository;
    this.permissionService = permissionService;
    this.messageService = messageService;
    this.socketServer = socketServer;
    this.userRepository = userRepository;
    this.notificationService = notificationService;
  }

  @Operation(
      summary = "All Meetings of Group",
      description = "Get all meetings of group",
      tags = "Meeting APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Get successfully",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = Meeting.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication")
  })
  @GetMapping(value = {""})
  public ResponseEntity<List<MeetingResponse>> all(
      @Parameter(hidden = true) @CurrentUser UserPrincipal user, @RequestParam String groupId) {
    if (!groupService.isGroupMember(groupId, user.getId())) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(meetingService.getMeetingGroup(groupId));
  }

  @Operation(
      summary = "Create New Meeting in Group",
      description = "Create a new meeting in group",
      tags = "Meeting APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Created successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication")
  })
  @PostMapping(value = {""})
  public APIResponse<Meeting> create(
      @Parameter(hidden = true) @CurrentUser UserPrincipal user,
      @RequestBody CreateMeetingRequest request) {
    Meeting newMeeting = meetingService.createNewMeeting(request);
    if (newMeeting == null) {
      return new APIResponse<>(false, "Not found organizer", 400);
    }
    return APIResponse.success(newMeeting);
  }

  @Operation(
      summary = "Update Meeting in Group",
      description = "Update a existing meeting in group",
      tags = "Meeting APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Updated successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication")
  })
  @PatchMapping("/{meetingId}")
  public APIResponse<Meeting> update(
      @Parameter(hidden = true) @CurrentUser UserPrincipal user,
      @PathVariable String meetingId,
      @RequestBody UpdateMeetingRequest request) {
    Meeting meeting = meetingService.updateMeeting(user.getId(), meetingId, request);
    if (meeting == null) {
      return APIResponse.notFound(404);
    }
    return APIResponse.success(meeting);
  }

  @Operation(
      summary = "Delete Meeting in Group",
      description = "Remove a existing meeting in group",
      tags = "Meeting APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Removed successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication")
  })
  @DeleteMapping("/{meetingId}")
  public APIResponse<String> delete(
      @Parameter(hidden = true) @CurrentUser UserPrincipal user, @PathVariable String meetingId) {
    if (!meetingRepository.existsById(meetingId)) {
      return APIResponse.notFound(404);
    }
    meetingService.deleteMeeting(meetingId);
    return APIResponse.success("OK");
  }

  @Operation(
      summary = "Get meeting detail of group",
      description = "Retrieve detail information of meeting group",
      tags = "Meeting APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Removed successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication")
  })
  @GetMapping("/{meetingId}")
  public APIResponse<MeetingDetailResponse> get(
      @Parameter(hidden = true) @CurrentUser UserPrincipal user, @PathVariable String meetingId) {
    MeetingDetailResponse response = meetingService.getMeetingById(user.getId(), meetingId);
    return APIResponse.success(response);
  }

  @Operation(
      summary = "Get meeting attendees of group",
      description = "Retrieve all information about attendess of meeting group",
      tags = "Meeting APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Removed successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication")
  })
  @GetMapping("/{meetingId}/attendees")
  public APIResponse<List<MeetingAttendeeResponse>> getAttendees(
      @Parameter(hidden = true) @CurrentUser UserPrincipal user, @PathVariable String meetingId) {
    return APIResponse.success(meetingService.getMeetingAttendees(meetingId));
  }

  @Operation(
      summary = "Reschedule Meeting in Group",
      description = "Reschedule a existing meeting to another time in group",
      tags = "Meeting APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Reschedule successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication")
  })
  @PatchMapping("/{meetingId}/reschedule")
  public APIResponse<Meeting> reschedule(
      @Parameter(hidden = true) @CurrentUser UserPrincipal user,
      @PathVariable String meetingId,
      @RequestBody RescheduleMeetingRequest request) {
    Meeting meeting = meetingService.rescheduleMeeting(user.getId(), meetingId, request);
    notificationService.sendRescheduleMeetingNotification(user.getId(), meeting, request);
    if (meeting == null) {
      return APIResponse.notFound(404);
    }
    return APIResponse.success(meeting);
  }
}
