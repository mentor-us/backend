package com.hcmus.mentor.backend.controller;

import com.hcmus.mentor.backend.controller.payload.ApiResponseDto;
import com.hcmus.mentor.backend.controller.payload.request.RescheduleMeetingRequest;
import com.hcmus.mentor.backend.controller.payload.request.meetings.CreateMeetingRequest;
import com.hcmus.mentor.backend.controller.payload.request.meetings.UpdateMeetingRequest;
import com.hcmus.mentor.backend.controller.payload.response.meetings.MeetingAttendeeResponse;
import com.hcmus.mentor.backend.controller.payload.response.meetings.MeetingDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.meetings.MeetingResponse;
import com.hcmus.mentor.backend.domain.Meeting;
import com.hcmus.mentor.backend.repository.MeetingRepository;
import com.hcmus.mentor.backend.security.principal.CurrentUser;
import com.hcmus.mentor.backend.security.principal.userdetails.CustomerUserDetails;
import com.hcmus.mentor.backend.service.GroupService;
import com.hcmus.mentor.backend.service.MeetingService;
import com.hcmus.mentor.backend.service.NotificationService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Meeting controller.
 */
@Tag(name = "meetings")
@RestController
@RequestMapping("api/meetings")
@SecurityRequirement(name = "bearer")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;
    private final GroupService groupService;
    private final MeetingRepository meetingRepository;
    private final NotificationService notificationService;

    /**
     * Retrieve all meetings of a group.
     *
     * @param user    The current user's principal information.
     * @param groupId The ID of the group.
     * @return ResponseEntity containing a list of MeetingResponse.
     */
    @GetMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<List<MeetingResponse>> all(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails user,
            @RequestParam String groupId) {
        if (!groupService.isGroupMember(groupId, user.getId())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(meetingService.getMeetingGroup(groupId));
    }

    /**
     * Create a new meeting in a group.
     *
     * @param user    The current user's principal information.
     * @param request The request payload for creating a new meeting.
     * @return APIResponse containing the created Meeting.
     */
    @PostMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Meeting> create(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails user,
            @RequestBody CreateMeetingRequest request) {
        Meeting newMeeting = meetingService.createNewMeeting(request);
        if (newMeeting == null) {
            return new ApiResponseDto<>(false, "Not found organizer", 400);
        }
        return ApiResponseDto.success(newMeeting);
    }

    /**
     * Update an existing meeting in a group.
     *
     * @param user      The current user's principal information.
     * @param meetingId The ID of the meeting to be updated.
     * @param request   The request payload for updating the meeting.
     * @return APIResponse containing the updated Meeting.
     */
    @PatchMapping("{meetingId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Meeting> update(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails user,
            @PathVariable String meetingId,
            @RequestBody UpdateMeetingRequest request) {
        Meeting meeting = meetingService.updateMeeting(user.getId(), meetingId, request);
        if (meeting == null) {
            return ApiResponseDto.notFound(404);
        }
        return ApiResponseDto.success(meeting);
    }

    /**
     * Remove an existing meeting from a group.
     *
     * @param user      The current user's principal information.
     * @param meetingId The ID of the meeting to be deleted.
     * @return APIResponse indicating the success or failure of the operation.
     */
    @DeleteMapping("{meetingId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<String> delete(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails user, @PathVariable String meetingId) {
        if (!meetingRepository.existsById(meetingId)) {
            return ApiResponseDto.notFound(404);
        }
        meetingService.deleteMeeting(meetingId);
        return ApiResponseDto.success("OK");
    }

    /**
     * Retrieve detailed information about a meeting in a group.
     *
     * @param user      The current user's principal information.
     * @param meetingId The ID of the meeting.
     * @return APIResponse containing MeetingDetailResponse.
     */
    @GetMapping("{meetingId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<MeetingDetailResponse> get(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails user, @PathVariable String meetingId) {
        MeetingDetailResponse response = meetingService.getMeetingById(user.getId(), meetingId);
        return ApiResponseDto.success(response);
    }

    /**
     * Retrieve information about attendees of a meeting in a group.
     *
     * @param user      The current user's principal information.
     * @param meetingId The ID of the meeting.
     * @return APIResponse containing a list of MeetingAttendeeResponse.
     */
    @GetMapping("{meetingId}/attendees")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<List<MeetingAttendeeResponse>> getAttendees(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails user, @PathVariable String meetingId) {
        return ApiResponseDto.success(meetingService.getMeetingAttendees(meetingId));
    }

    /**
     * Reschedule an existing meeting to another time in a group.
     *
     * @param user      The current user's principal information.
     * @param meetingId The ID of the meeting to be rescheduled.
     * @param request   The request payload for rescheduling the meeting.
     * @return APIResponse containing the rescheduled Meeting.
     */
    @PatchMapping("{meetingId}/reschedule")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Meeting> reschedule(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails user,
            @PathVariable String meetingId,
            @RequestBody RescheduleMeetingRequest request) {
        Meeting meeting = meetingService.rescheduleMeeting(user.getId(), meetingId, request);
        notificationService.sendRescheduleMeetingNotification(user.getId(), meeting, request);
        if (meeting == null) {
            return ApiResponseDto.notFound(404);
        }
        return ApiResponseDto.success(meeting);
    }
}
