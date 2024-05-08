package com.hcmus.mentor.backend.controller;

import an.awesome.pipelinr.Pipeline;
import com.hcmus.mentor.backend.controller.payload.ApiResponseDto;
import com.hcmus.mentor.backend.controller.payload.request.groups.AddMembersRequest;
import com.hcmus.mentor.backend.controller.payload.request.groups.CreateGroupRequest;
import com.hcmus.mentor.backend.controller.payload.request.groups.UpdateGroupRequest;
import com.hcmus.mentor.backend.controller.payload.response.HomePageResponse;
import com.hcmus.mentor.backend.controller.payload.response.ShortMediaMessage;
import com.hcmus.mentor.backend.controller.payload.response.channel.ChannelForwardResponse;
import com.hcmus.mentor.backend.controller.payload.response.groups.GroupDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.groups.GroupHomepageResponse;
import com.hcmus.mentor.backend.controller.payload.response.groups.GroupMembersResponse;
import com.hcmus.mentor.backend.controller.payload.response.groups.UpdateGroupAvatarResponse;
import com.hcmus.mentor.backend.controller.usecase.channel.getchannelforward.GetChannelsForwardCommand;
import com.hcmus.mentor.backend.controller.usecase.group.creategroup.CreateGroupCommand;
import com.hcmus.mentor.backend.controller.usecase.group.findowngroups.FindOwnGroupsCommand;
import com.hcmus.mentor.backend.controller.usecase.group.getgroupworkspace.GetGroupWorkSpaceQuery;
import com.hcmus.mentor.backend.controller.usecase.group.importgroup.ImportGroupCommand;
import com.hcmus.mentor.backend.controller.usecase.group.togglemarkmentee.ToggleMarkMenteeCommand;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.CurrentUser;
import com.hcmus.mentor.backend.security.principal.userdetails.CustomerUserDetails;
import com.hcmus.mentor.backend.service.EventService;
import com.hcmus.mentor.backend.service.GroupService;
import com.hcmus.mentor.backend.service.PermissionService;
import com.hcmus.mentor.backend.service.dto.EventDto;
import com.hcmus.mentor.backend.service.dto.GroupServiceDto;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static com.hcmus.mentor.backend.controller.payload.returnCode.UserReturnCode.NOT_FOUND;

/**
 * Group controller.
 */
@Tag(name = "groups")
@RestController
@RequestMapping("api/groups")
@SecurityRequirement(name = "bearer")
@RequiredArgsConstructor
public class GroupController {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupService groupService;
    private final EventService eventService;
    private final PermissionService permissionService;
    private final Pipeline pipeline;

    /**
     * Retrieves groups based on the user's role and group type.
     * Admins can get all groups (Paging), while users can get mentee groups or mentor groups (Paging).
     *
     * @param customerUserDetails The current user's principal information.
     * @param page                The page number for pagination.
     * @param pageSize            The number of items per page.
     * @param type                The type of groups to retrieve ("admin" for all groups).
     * @return APIResponse containing a Page of Group entities based on the specified criteria.
     */
    @GetMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Page<Group>> all(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int pageSize,
            @RequestParam(defaultValue = "") String type) {
        Page<Group> groups = new PageImpl<>(new ArrayList<>());
        if (!type.equals("admin")) {
            return ApiResponseDto.success(pagingResponse(groups));
        }

        boolean isSuperAdmin = permissionService.isSuperAdmin(customerUserDetails.getEmail());
        Pageable pageRequest = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate"));
        if (isSuperAdmin) {
            groups = groupRepository.findAll(pageRequest);
        } else {
            var user = userRepository.findByEmail(customerUserDetails.getEmail());
            if (user.isEmpty()) {
                return ApiResponseDto.notFound(NOT_FOUND);
            }
            String creatorId = user.get().getId();
            groups = groupRepository.findAllByCreatorId(pageRequest, creatorId);
        }

        groups = new PageImpl<>(groupService.validateTimeGroups(groups.getContent()), pageRequest, groups.getTotalElements());
        return ApiResponseDto.success(pagingResponse(groups));
    }


    /**
     * Retrieves the user's own groups based on the specified type (mentor, mentee, or all).
     *
     * @param customerUserDetails The current user's principal information.
     * @param page                The page number for pagination.
     * @param pageSize            The number of items per page.
     * @param type                The type of groups to retrieve ("mentor", "mentee", or empty for all).
     * @return APIResponse containing a Page of GroupHomepageResponse entities.
     */
    @GetMapping("own")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Page<GroupHomepageResponse>> getOwnGroups(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int pageSize,
            @RequestParam(defaultValue = "") String type) {
        var command = new FindOwnGroupsCommand(customerUserDetails.getId(), null, page, pageSize);
        switch (type) {
            case "mentor":
                command.setIsMentor(true);
                break;
            case "mentee":
                command.setIsMentor(false);
                break;
            default:
                command.setIsMentor(null);
                break;
        }
        return ApiResponseDto.success(pagingHomepageResponse(pipeline.send(command)));
    }


    /**
     * Retrieves recent groups of any user based on their last update.
     *
     * @param customerUserDetails The current user's principal information.
     * @param page                The page number for pagination.
     * @param pageSize            The number of items per page.
     * @return APIResponse containing a Page of Group entities.
     */
    @GetMapping("recent")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Page<Group>> recentGroups(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int pageSize) {
        Page<Group> groups = groupService.findRecentGroupsOfUser(customerUserDetails.getId(), page, pageSize);
        return ApiResponseDto.success(pagingResponse(groups));
    }

    /**
     * Retrieves an existing group by its ID.
     *
     * @param id The ID of the group to retrieve.
     * @return APIResponse containing the retrieved Group entity or a not found response.
     */
    @GetMapping("{id}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<GroupDetailResponse> get(
            @PathVariable("id") String id) {
        Optional<Group> groupWrapper = groupRepository.findById(id);
        if (groupWrapper.isPresent()) {
            return ApiResponseDto.success(new GroupDetailResponse(groupWrapper.get()));
        }
        return ApiResponseDto.notFound(NOT_FOUND);
    }

    /**
     * Creates a new group (Only Admins).
     *
     * @param loggedUser The current user's principal information.
     * @param command    The request body containing information to create a new group.
     * @return APIResponse containing the created Group entity or an error response.
     */
    @PostMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Group> create(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails loggedUser,
            @RequestBody CreateGroupRequest command) {
        String creatorEmail = loggedUser.getEmail();
        GroupServiceDto groupReturn = pipeline.send(new CreateGroupCommand(creatorEmail, command));
        return new ApiResponseDto(groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    /**
     * Imports multiple groups by a template file.
     *
     * @param customerUserDetails The current user's principal information.
     * @param file                The template file containing group information.
     * @return APIResponse containing a list of imported Group entities or an error response.
     * @throws IOException If an I/O error occurs during the import process.
     */
    @PostMapping(value = "import", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<List<Group>> importGroups(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam("file") MultipartFile file)
            throws IOException {
        String email = customerUserDetails.getEmail();
        GroupServiceDto groupReturn = pipeline.send(new ImportGroupCommand(email, file));
        return new ApiResponseDto(groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    /**
     * Finds groups with multiple filters.
     *
     * @param customerUserDetails The current user's principal information.
     * @param name                The name filter for groups.
     * @param mentorEmail         The mentor's email filter for groups.
     * @param menteeEmail         The mentee's email filter for groups.
     * @param groupCategory       The group category filter for groups.
     * @param timeStart1          The start time filter for groups (first range).
     * @param timeEnd1            The end time filter for groups (first range).
     * @param timeStart2          The start time filter for groups (second range).
     * @param timeEnd2            The end time filter for groups (second range).
     * @param status              The status filter for groups.
     * @param page                The page number for pagination.
     * @param size                The number of items per page.
     * @return APIResponse containing a Page of Group entities based on the specified criteria.
     * @throws InvocationTargetException If an invocation target exception occurs during the method invocation.
     * @throws NoSuchMethodException     If a method is not found during reflection.
     * @throws IllegalAccessException    If access to the method is not allowed.
     */
    @GetMapping("find")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Page<Group>> get(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "") String mentorEmail,
            @RequestParam(defaultValue = "") String menteeEmail,
            @RequestParam(defaultValue = "") String groupCategory,
            @RequestParam(defaultValue = "1970-01-01T00:00:00")
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            Date timeStart1,
            @RequestParam(defaultValue = "2300-01-01T00:00:00")
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            Date timeEnd1,
            @RequestParam(defaultValue = "1970-01-01T00:00:00")
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            Date timeStart2,
            @RequestParam(defaultValue = "2300-01-01T00:00:00")
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            Date timeEnd2,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "25") Integer size)
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        String email = customerUserDetails.getEmail();
        GroupServiceDto groupReturn = groupService.findGroups(
                email,
                name,
                mentorEmail,
                menteeEmail,
                groupCategory,
                timeStart1,
                timeEnd1,
                timeStart2,
                timeEnd2,
                status,
                page,
                size);
        return new ApiResponseDto(
                pagingResponse((Page<Group>) groupReturn.getData()),
                groupReturn.getReturnCode(),
                groupReturn.getMessage());
    }

    /**
     * Adds mentees to a group.
     *
     * @param customerUserDetails The current user's principal information.
     * @param groupId             The ID of the group to which mentees will be added.
     * @param request             The request body containing mentee information.
     * @return APIResponse containing the updated Group entity or an error response.
     */
    @PostMapping("{groupId}/mentees")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Group> addMentees(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable("groupId") String groupId,
            @RequestBody AddMembersRequest request) {
        String email = customerUserDetails.getEmail();
        GroupServiceDto groupReturn = groupService.addMembers(email, groupId, request, false);
        return new ApiResponseDto(
                groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    /**
     * Adds mentors to a group.
     *
     * @param customerUserDetails The current user's principal information.
     * @param groupId             The ID of the group to which mentors will be added.
     * @param request             The request body containing mentor information.
     * @return APIResponse containing the updated Group entity or an error response.
     */
    @PostMapping("{groupId}/mentors")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Group> addMentors(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable("groupId") String groupId,
            @RequestBody AddMembersRequest request) {
        String email = customerUserDetails.getEmail();
        GroupServiceDto groupReturn = groupService.addMembers(email, groupId, request, true);
        return new ApiResponseDto(
                groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    /**
     * Deletes a mentee from a group.
     *
     * @param customerUserDetails The current user's principal information.
     * @param groupId             The ID of the group from which the mentee will be deleted.
     * @param menteeId            The ID of the mentee to be deleted from the group.
     * @return APIResponse indicating the success or failure of the operation.
     */
    @DeleteMapping("{groupId}/mentees/{menteeId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto deleteMentee(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable("groupId") String groupId,
            @PathVariable("menteeId") String menteeId) {
        String email = customerUserDetails.getEmail();
        GroupServiceDto groupReturn = groupService.deleteMentee(email, groupId, menteeId);
        return new ApiResponseDto(
                groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    /**
     * Deletes a mentor from a group.
     *
     * @param customerUserDetails The current user's principal information.
     * @param groupId             The ID of the group from which the mentor will be deleted.
     * @param mentorId            The ID of the mentor to be deleted from the group.
     * @return APIResponse indicating the success or failure of the operation.
     */
    @DeleteMapping("{groupId}/mentors/{mentorId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto deleteMentor(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable("groupId") String groupId,
            @PathVariable("mentorId") String mentorId) {
        String email = customerUserDetails.getEmail();
        GroupServiceDto groupReturn = groupService.deleteMentor(email, groupId, mentorId);
        return new ApiResponseDto(
                groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    /**
     * Promotes a mentee to a mentor within a group.
     *
     * @param customerUserDetails The current user's principal information.
     * @param groupId             The ID of the group in which the promotion will occur.
     * @param menteeId            The ID of the mentee to be promoted to mentor.
     * @return APIResponse indicating the success or failure of the promotion.
     */
    @PatchMapping("{groupId}/mentors/{menteeId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto promoteToMentor(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable("groupId") String groupId,
            @PathVariable("menteeId") String menteeId) {
        String email = customerUserDetails.getEmail();
        GroupServiceDto groupReturn = groupService.promoteToMentor(email, groupId, menteeId);
        return new ApiResponseDto(
                groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    /**
     * Demotes a mentor to a mentee within a group.
     *
     * @param customerUserDetails The current user's principal information.
     * @param groupId             The ID of the group in which the demotion will occur.
     * @param mentorId            The ID of the mentor to be demoted to mentee.
     * @return APIResponse indicating the success or failure of the demotion.
     */
    @PatchMapping("{groupId}/mentees/{mentorId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto demoteToMentee(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable("groupId") String groupId,
            @PathVariable("mentorId") String mentorId) {
        String email = customerUserDetails.getEmail();
        GroupServiceDto groupReturn = groupService.demoteToMentee(email, groupId, mentorId);
        return new ApiResponseDto(
                groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    /**
     * Retrieves the template import file for groups.
     *
     * @return ResponseEntity containing the template file for group import.
     * @throws Exception If an exception occurs during the process.
     */
    @GetMapping("import")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Resource> getTemplate() throws Exception {
        InputStream generateTemplateStream = groupService.loadTemplate("/templates/temp-import-groups.xlsx");

        Resource resource = new InputStreamResource(generateTemplateStream);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + "import-groups.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .contentLength(generateTemplateStream.available())
                .body(resource);
    }

    /**
     * Deletes a group.
     *
     * @param customerUserDetails The current user's principal information.
     * @param id                  The ID of the group to be deleted.
     * @return APIResponse indicating the success or failure of the group deletion.
     */
    @DeleteMapping(value = "{id}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto delete(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails, @PathVariable String id) {
        String email = customerUserDetails.getEmail();
        GroupServiceDto groupReturn = groupService.deleteGroup(email, id);
        return new ApiResponseDto(
                groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    /**
     * Updates a group's information.
     *
     * @param customerUserDetails The current user's principal information.
     * @param id                  The ID of the group to be updated.
     * @param request             The request body containing the updated information.
     * @return APIResponse containing the updated Group entity or an error response.
     */
    @PatchMapping("{id}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Group> update(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable String id,
            @RequestBody UpdateGroupRequest request) {
        String email = customerUserDetails.getEmail();
        GroupServiceDto groupReturn = groupService.updateGroup(email, id, request);
        return new ApiResponseDto(
                groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    /**
     * Retrieves data for the homepage of the mobile app.
     *
     * @param customerUserDetails The current user's principal information.
     * @return APIResponse containing data for the homepage.
     */
    @GetMapping("home")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<HomePageResponse> getHomePage(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails) {
        String userId = customerUserDetails.getId();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ApiResponseDto.notFound(404);
        }
        List<EventDto> events = eventService.getMostRecentEvents(userId);
        List<GroupHomepageResponse> pinnedGroups = groupService.getUserPinnedGroups(userId);
        Slice<GroupHomepageResponse> groups = groupService.getHomePageRecentGroupsOfUser(userId, 0, 25);
        return ApiResponseDto.success(new HomePageResponse(events, pinnedGroups, groups));
    }

    /**
     * Deletes multiple groups.
     *
     * @param customerUserDetails The current user's principal information.
     * @param ids                 The list of group IDs to be deleted.
     * @return APIResponse indicating the success or failure of the group deletion.
     */
    @DeleteMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto deleteMultiple(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestBody List<String> ids) {
        String email = customerUserDetails.getEmail();
        GroupServiceDto groupReturn = groupService.deleteMultiple(email, ids);
        return new ApiResponseDto(
                groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    /**
     * Disables multiple groups.
     *
     * @param customerUserDetails The current user's principal information.
     * @param ids                 The list of group IDs to be disabled.
     * @return APIResponse indicating the success or failure of disabling the groups.
     */
    @PatchMapping(value = "disable")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto disableMultiple(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestBody List<String> ids) {
        String email = customerUserDetails.getEmail();
        GroupServiceDto groupReturn = groupService.disableMultiple(email, ids);
        return new ApiResponseDto(
                groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    /**
     * Enables multiple groups, checking time start and time end to generate status.
     *
     * @param customerUserDetails The current user's principal information.
     * @param ids                 The list of group IDs to be enabled.
     * @return APIResponse indicating the success or failure of enabling the groups.
     */
    @PatchMapping(value = "enable")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto enableMultiple(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestBody List<String> ids) {
        String email = customerUserDetails.getEmail();
        GroupServiceDto groupReturn = groupService.enableMultiple(email, ids);
        return new ApiResponseDto(
                groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    /**
     * Get members of a group.
     *
     * @param customerUserDetails The current user's principal information.
     * @param groupId             The ID of the group for which members are requested.
     * @return APIResponse containing the group members' information.
     */
    @GetMapping("{id}/members")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<GroupMembersResponse> getGroupMembers(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable("id") String groupId) {
        GroupServiceDto groupMembers = groupService.getGroupMembers(groupId, customerUserDetails.getId());
        return new ApiResponseDto(
                groupMembers.getData(),
                groupMembers.getReturnCode(),
                groupMembers.getMessage());
    }

    /**
     * Pin a group for mobile users.
     *
     * @param customerUserDetails The current user's principal information.
     * @param groupId             The ID of the group to be pinned.
     * @return APIResponse indicating the success or failure of the operation.
     */
    @PostMapping("{id}/pin")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Object> pinGroup(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable("id") String groupId) {
        groupService.pinGroup(customerUserDetails.getId(), groupId);
        return new ApiResponseDto(true, "OK", 200);
    }

    /**
     * Unpin a group for mobile users.
     *
     * @param customerUserDetails The current user's principal information.
     * @param groupId             The ID of the group to be unpinned.
     * @return APIResponse indicating the success or failure of the operation.
     */
    @PostMapping("{id}/unpin")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Object> unpinGroup(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable("id") String groupId) {
        groupService.unpinGroup(customerUserDetails.getId(), groupId);
        return new ApiResponseDto(true, "OK", 200);
    }

    /**
     * (Use /api/channels/{id}) Get detailed information about a channel.
     *
     * @param customerUserDetails The current user's principal information.
     * @param groupId             The ID of the group for which details are requested.
     * @return APIResponse containing detailed information about the group.
     */
    @Deprecated(forRemoval = true)
    @GetMapping("{id}/detail")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<GroupDetailResponse> getGroup(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable("id") String groupId) {
        GroupServiceDto groupData = groupService.getGroupDetail(customerUserDetails.getId(), groupId);
        return new ApiResponseDto(groupData.getData(), groupData.getReturnCode(), groupData.getMessage());
    }

    /**
     * (Use /api/channels/{id}/media) Get media (images and files) of a group for mobile users.
     *
     * @param customerUserDetails The current user's principal information.
     * @param groupId             The ID of the group for which media is requested.
     * @return APIResponse containing media information of the group.
     */
    @Deprecated(forRemoval = true)
    @GetMapping("{id}/media")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<ShortMediaMessage> getGroupMedia(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable("id") String groupId) {
        GroupServiceDto groupData = groupService.getGroupMedia(customerUserDetails.getId(), groupId);
        return new ApiResponseDto(groupData.getData(), groupData.getReturnCode(), groupData.getMessage());
    }

    /**
     * Update the avatar of a group for mobile users.
     *
     * @param customerUserDetails The current user's principal information.
     * @param groupId             The ID of the group for which the avatar is updated.
     * @param file                The multipart file containing the new avatar.
     * @return APIResponse containing the updated avatar information.
     */
    @SneakyThrows
    @PostMapping(value = "{groupId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<UpdateGroupAvatarResponse> updateGroupAvatar(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable String groupId,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        UpdateGroupAvatarResponse groupData = groupService.updateAvatar(customerUserDetails.getId(), groupId, file);
        return ResponseEntity.ok(groupData);
    }

    /**
     * Export the table of groups.
     *
     * @param customerUserDetails The current user's principal information.
     * @param remainColumns       List of columns to remain in the export.
     * @return ResponseEntity containing the exported group table.
     * @throws IOException If an I/O exception occurs during the export process.
     */
    @GetMapping(value = "export")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Resource> export(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam(defaultValue = "") List<String> remainColumns)
            throws IOException {
        return groupService.generateExportTable(customerUserDetails.getEmail(), remainColumns);
    }

    /**
     * Export the table of mentors in a group.
     *
     * @param customerUserDetails The current user's principal information.
     * @param groupId             The ID of the group for which mentors are exported.
     * @param remainColumns       List of columns to remain in the export.
     * @return ResponseEntity containing the exported mentors' group table.
     * @throws IOException If an I/O exception occurs during the export process.
     */
    @GetMapping(value = "{groupId}/mentors/export")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Resource> exportMentors(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable String groupId,
            @RequestParam(defaultValue = "") List<String> remainColumns)
            throws IOException {
        return groupService.generateExportTableMembers(customerUserDetails.getEmail(), remainColumns, groupId, "MENTOR");
    }

    /**
     * Export the table of mentees in a group.
     *
     * @param customerUserDetails The current user's principal information.
     * @param groupId             The ID of the group for which mentees are exported.
     * @param remainColumns       List of columns to remain in the export.
     * @return ResponseEntity containing the exported mentees' group table.
     * @throws IOException If an I/O exception occurs during the export process.
     */
    @GetMapping(value = "{groupId}/mentees/export")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Resource> exportMentees(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable String groupId,
            @RequestParam(defaultValue = "") List<String> remainColumns)
            throws IOException {
        return groupService.generateExportTableMembers(customerUserDetails.getEmail(), remainColumns, groupId, "MENTEE");
    }

    /**
     * Pin a message for mobile users.
     *
     * @param customerUserDetails The current user's principal information.
     * @param groupId             The ID of the group where the message is pinned.
     * @param messageId           The ID of the message to be pinned.
     * @return ResponseEntity indicating the success or failure of the operation.
     */
    @PostMapping("{groupId}/pin-message")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Void> pinMessage(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable String groupId,
            @RequestParam String messageId) {
        groupService.pinChannelMessage(customerUserDetails.getId(), groupId, messageId);
        return ResponseEntity.ok().build();
    }

    /**
     * Unpin a message for mobile users.
     *
     * @param customerUserDetails The current user's principal information.
     * @param groupId             The ID of the group where the message is unpinned.
     * @param messageId           The ID of the message to be unpinned.
     * @return ResponseEntity indicating the success or failure of the operation.
     */
    @PostMapping("{groupId}/unpin-message")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Resource> unpinMessage(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable String groupId,
            @RequestParam String messageId) {
        groupService.unpinChannelMessage(customerUserDetails.getId(), groupId, messageId);
        return ResponseEntity.ok().build();
    }

    /**
     * Export groups table based on search conditions.
     *
     * @param customerUserDetails The current user's principal information.
     * @param name                The name to search for in groups.
     * @param mentorEmail         The email of the mentor to search for in groups.
     * @param menteeEmail         The email of the mentee to search for in groups.
     * @param groupCategory       The category of the group to search for.
     * @param timeStart1          The start time of the first time range.
     * @param timeEnd1            The end time of the first time range.
     * @param timeStart2          The start time of the second time range.
     * @param timeEnd2            The end time of the second time range.
     * @param status              The status to search for in groups.
     * @param remainColumns       List of columns to remain in the export.
     * @return ResponseEntity containing the exported group table based on search conditions.
     * @throws IOException If an I/O exception occurs during the export process.
     */
    @GetMapping(value = "export/search")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Resource> exportBySearchConditions(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "") String mentorEmail,
            @RequestParam(defaultValue = "") String menteeEmail,
            @RequestParam(defaultValue = "") String groupCategory,
            @RequestParam(defaultValue = "1970-01-01T00:00:00")
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            Date timeStart1,
            @RequestParam(defaultValue = "2300-01-01T00:00:00")
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            Date timeEnd1,
            @RequestParam(defaultValue = "1970-01-01T00:00:00")
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            Date timeStart2,
            @RequestParam(defaultValue = "2300-01-01T00:00:00")
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            Date timeEnd2,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "") List<String> remainColumns)
            throws IOException {
        return groupService.generateExportTableBySearchConditions(
                customerUserDetails.getEmail(),
                name,
                mentorEmail,
                menteeEmail,
                groupCategory,
                timeStart1,
                timeEnd1,
                timeStart2,
                timeEnd2,
                status,
                remainColumns);
    }

    /**
     * Get the central workspace of a group for mobile users.
     *
     * @param customerUserDetails The current user's principal information.
     * @param groupId             The ID of the group for which the workspace is requested.
     * @return ResponseEntity containing the group workspace information.
     */
    @GetMapping("{groupId}/workspace")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<GroupDetailResponse> getWorkspace(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable String groupId) {
        var response = pipeline.send(new GetGroupWorkSpaceQuery(customerUserDetails.getId(), groupId));
        return ResponseEntity.ok(response);
    }

    /**
     * Mark a mentee in a group for mobile users.
     *
     * @param customerUserDetails The current user's principal information.
     * @param groupId             The ID of the group where the mentee is marked.
     * @param menteeId            The ID of the mentee to be marked.
     * @return ResponseEntity indicating the success or failure of the operation.
     */
    @PostMapping("{groupId}/star")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Void> markMentee(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable String groupId,
            @RequestParam String menteeId) {
        pipeline.send(new ToggleMarkMenteeCommand(customerUserDetails.getId(), groupId, menteeId, true));
        return ResponseEntity.ok().build();
    }

    /**
     * Unmark a mentee in a group for mobile users.
     *
     * @param customerUserDetails The current user's principal information.
     * @param groupId             The ID of the group where the mentee is unmarked.
     * @param menteeId            The ID of the mentee to be unmarked.
     * @return ResponseEntity indicating the success or failure of the operation.
     */
    @DeleteMapping("{groupId}/star")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Void> unmarkMentee(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable String groupId,
            @RequestParam String menteeId) {
        pipeline.send(new ToggleMarkMenteeCommand(customerUserDetails.getId(), groupId, menteeId, false));
        return ResponseEntity.ok().build();
    }

    /**
     * Get the list of group forwards for mobile users.
     *
     * @param customerUserDetails The current user's principal information.
     * @param name                Optional name parameter for filtering the list.
     * @return ResponseEntity containing the list of group forwards.
     */
    @GetMapping("forward")
    @SneakyThrows
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<List<ChannelForwardResponse>> getListGroupForward(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails, @RequestParam Optional<String> name) {

        return ResponseEntity.ok(pipeline.send(new GetChannelsForwardCommand(customerUserDetails.getId(), name.orElse(""))));
    }

    private Map<String, Object> pagingResponse(Page<Group> groups) {
        Map<String, Object> response = new HashMap<>();
        response.put("groups", groups.getContent());
        response.put("currentPage", groups.getNumber());
        response.put("totalItems", groups.getTotalElements());
        response.put("totalPages", groups.getTotalPages());
        return response;
    }

    private Map<String, Object> pagingHomepageResponse(Page<GroupHomepageResponse> groups) {
        Map<String, Object> response = new HashMap<>();
        response.put("groups", groups.getContent());
        response.put("currentPage", groups.getNumber());
        response.put("totalItems", groups.getTotalElements());
        response.put("totalPages", groups.getTotalPages());
        return response;
    }
}