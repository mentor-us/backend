package com.hcmus.mentor.backend.controller;

import com.hcmus.mentor.backend.controller.payload.ApiResponseDto;
import com.hcmus.mentor.backend.controller.payload.request.groups.AddMenteesRequest;
import com.hcmus.mentor.backend.controller.payload.request.groups.AddMentorsRequest;
import com.hcmus.mentor.backend.controller.payload.request.groups.CreateGroupRequest;
import com.hcmus.mentor.backend.controller.payload.request.groups.UpdateGroupRequest;
import com.hcmus.mentor.backend.controller.payload.response.HomePageResponse;
import com.hcmus.mentor.backend.controller.payload.response.ShortMediaMessage;
import com.hcmus.mentor.backend.controller.payload.response.channel.ChannelForwardResponse;
import com.hcmus.mentor.backend.controller.payload.response.groups.GroupDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.groups.GroupHomepageResponse;
import com.hcmus.mentor.backend.controller.payload.response.groups.GroupMembersResponse;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.CurrentUser;
import com.hcmus.mentor.backend.security.UserPrincipal;
import com.hcmus.mentor.backend.service.EventService;
import com.hcmus.mentor.backend.service.GroupService;
import com.hcmus.mentor.backend.service.GroupService.GroupReturnService;
import com.hcmus.mentor.backend.service.PermissionService;
import io.minio.errors.*;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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

    private static final Logger logger = LogManager.getLogger(GroupController.class);
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupService groupService;
    private final EventService eventService;
    private final PermissionService permissionService;
    private static final String TEMPLATE_PATH = "src/main/resources/templates/import-groups.xlsx";
    private static final String TEMP_TEMPLATE_PATH = "src/main/resources/templates/temp-import-groups.xlsx";

    /**
     * Retrieves groups based on the user's role and group type.
     * Admins can get all groups (Paging), while users can get mentee groups or mentor groups (Paging).
     *
     * @param userPrincipal The current user's principal information.
     * @param page          The page number for pagination.
     * @param pageSize      The number of items per page.
     * @param type          The type of groups to retrieve ("admin" for all groups).
     * @return APIResponse containing a Page of Group entities based on the specified criteria.
     */
    @GetMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Page<Group>> all(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int pageSize,
            @RequestParam(defaultValue = "") String type) {
        Page<Group> groups = new PageImpl<>(new ArrayList<>());
        if (!type.equals("admin")) {
            return ApiResponseDto.success(pagingResponse(groups));
        }

        boolean isSuperAdmin = permissionService.isSuperAdmin(userPrincipal.getEmail());
        Pageable pageRequest =
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate"));
        if (isSuperAdmin) {
            groups = groupRepository.findAll(pageRequest);
        } else {
            String creatorId = userRepository.findByEmail(userPrincipal.getEmail()).get().getId();
            groups = groupRepository.findAllByCreatorId(pageRequest, creatorId);
        }
        for (Group group : groups) {
            if (group.getStatus() != GroupStatus.DELETED && group.getStatus() != GroupStatus.DISABLED) {
                if (group.getTimeEnd().before(new Date())) {
                    group.setStatus(GroupStatus.OUTDATED);
                    groupRepository.save(group);
                }
                if (group.getTimeStart().after(new Date())) {
                    group.setStatus(GroupStatus.INACTIVE);
                    groupRepository.save(group);
                }
            }
        }
        return ApiResponseDto.success(pagingResponse(groups));
    }


    /**
     * Retrieves the user's own groups based on the specified type (mentor, mentee, or all).
     *
     * @param userPrincipal The current user's principal information.
     * @param page          The page number for pagination.
     * @param pageSize      The number of items per page.
     * @param type          The type of groups to retrieve ("mentor", "mentee", or empty for all).
     * @return APIResponse containing a Page of GroupHomepageResponse entities.
     */
    @GetMapping("own")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Page<GroupHomepageResponse>> getOwnGroups(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int pageSize,
            @RequestParam(defaultValue = "") String type) {
        Page<GroupHomepageResponse> groups;
        switch (type) {
            case "mentor":
                groups = groupService.findMentorGroups(userPrincipal.getId(), page, pageSize);
                break;
            case "mentee":
                groups = groupService.findMenteeGroups(userPrincipal.getId(), page, pageSize);
                break;
            default:
                groups = groupService.findOwnGroups(userPrincipal.getId(), page, pageSize);
                break;
        }
        var userOpt = userRepository.findById(userPrincipal.getId());
        var user = userOpt.orElse(null);
        for (GroupHomepageResponse group : groups) {
            boolean isPinned = user.isPinnedGroup(group.getId());
            group.setPinned(isPinned);
        }
        return ApiResponseDto.success(pagingHomepageResponse(groups));
    }


    /**
     * Retrieves recent groups of any user based on their last update.
     *
     * @param userPrincipal The current user's principal information.
     * @param page          The page number for pagination.
     * @param pageSize      The number of items per page.
     * @return APIResponse containing a Page of Group entities.
     */
    @GetMapping("recent")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Page<Group>> recentGroups(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int pageSize) {
        Page<Group> groups = groupService.findRecentGroupsOfUser(userPrincipal.getId(), page, pageSize);
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
    public ApiResponseDto<Group> get(
            @PathVariable("id") String id) {
        Optional<Group> groupWrapper = groupRepository.findById(id);
        return groupWrapper.map(ApiResponseDto::success).orElseGet(() -> ApiResponseDto.notFound(NOT_FOUND));
    }

    /**
     * Creates a new group (Only Admins).
     *
     * @param userPrincipal The current user's principal information.
     * @param request       The request body containing information to create a new group.
     * @return APIResponse containing the created Group entity or an error response.
     */
    @PostMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Group> create(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @RequestBody CreateGroupRequest request) {
        String email = userPrincipal.getEmail();
        GroupReturnService groupReturn = groupService.createNewGroup(email, request);
        return new ApiResponseDto(
                groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    /**
     * Imports multiple groups by a template file.
     *
     * @param userPrincipal The current user's principal information.
     * @param file          The template file containing group information.
     * @return APIResponse containing a list of imported Group entities or an error response.
     * @throws IOException If an I/O error occurs during the import process.
     */
    @PostMapping(value = "import", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<List<Group>> importGroups(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @RequestParam("file") MultipartFile file)
            throws IOException {
        String email = userPrincipal.getEmail();
        GroupReturnService groupReturn = groupService.importGroups(email, file);
        return new ApiResponseDto(
                groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    /**
     * Finds groups with multiple filters.
     *
     * @param userPrincipal The current user's principal information.
     * @param name          The name filter for groups.
     * @param mentorEmail   The mentor's email filter for groups.
     * @param menteeEmail   The mentee's email filter for groups.
     * @param groupCategory The group category filter for groups.
     * @param timeStart1    The start time filter for groups (first range).
     * @param timeEnd1      The end time filter for groups (first range).
     * @param timeStart2    The start time filter for groups (second range).
     * @param timeEnd2      The end time filter for groups (second range).
     * @param status        The status filter for groups.
     * @param page          The page number for pagination.
     * @param size          The number of items per page.
     * @return APIResponse containing a Page of Group entities based on the specified criteria.
     * @throws InvocationTargetException If an invocation target exception occurs during the method invocation.
     * @throws NoSuchMethodException     If a method is not found during reflection.
     * @throws IllegalAccessException    If access to the method is not allowed.
     */
    @GetMapping("find")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Page<Group>> get(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
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
        String email = userPrincipal.getEmail();
        GroupReturnService groupReturn = groupService.findGroups(
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
     * @param userPrincipal The current user's principal information.
     * @param groupId       The ID of the group to which mentees will be added.
     * @param request       The request body containing mentee information.
     * @return APIResponse containing the updated Group entity or an error response.
     */
    @PostMapping("{groupId}/mentees")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Group> addMentees(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable("groupId") String groupId,
            @RequestBody AddMenteesRequest request) {
        String email = userPrincipal.getEmail();
        GroupReturnService groupReturn = groupService.addMentees(email, groupId, request);
        return new ApiResponseDto(
                groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    /**
     * Adds mentors to a group.
     *
     * @param userPrincipal The current user's principal information.
     * @param groupId       The ID of the group to which mentors will be added.
     * @param request       The request body containing mentor information.
     * @return APIResponse containing the updated Group entity or an error response.
     */
    @PostMapping("{groupId}/mentors")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Group> addMentors(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable("groupId") String groupId,
            @RequestBody AddMentorsRequest request) {
        String email = userPrincipal.getEmail();
        GroupReturnService groupReturn = groupService.addMentors(email, groupId, request);
        return new ApiResponseDto(
                groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    /**
     * Deletes a mentee from a group.
     *
     * @param userPrincipal The current user's principal information.
     * @param groupId       The ID of the group from which the mentee will be deleted.
     * @param menteeId      The ID of the mentee to be deleted from the group.
     * @return APIResponse indicating the success or failure of the operation.
     */
    @DeleteMapping("{groupId}/mentees/{menteeId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto deleteMentee(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable("groupId") String groupId,
            @PathVariable("menteeId") String menteeId) {
        String email = userPrincipal.getEmail();
        GroupReturnService groupReturn = groupService.deleteMentee(email, groupId, menteeId);
        return new ApiResponseDto(
                groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    /**
     * Deletes a mentor from a group.
     *
     * @param userPrincipal The current user's principal information.
     * @param groupId       The ID of the group from which the mentor will be deleted.
     * @param mentorId      The ID of the mentor to be deleted from the group.
     * @return APIResponse indicating the success or failure of the operation.
     */
    @DeleteMapping("{groupId}/mentors/{mentorId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto deleteMentor(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable("groupId") String groupId,
            @PathVariable("mentorId") String mentorId) {
        String email = userPrincipal.getEmail();
        GroupReturnService groupReturn = groupService.deleteMentor(email, groupId, mentorId);
        return new ApiResponseDto(
                groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    /**
     * Promotes a mentee to a mentor within a group.
     *
     * @param userPrincipal The current user's principal information.
     * @param groupId       The ID of the group in which the promotion will occur.
     * @param menteeId      The ID of the mentee to be promoted to mentor.
     * @return APIResponse indicating the success or failure of the promotion.
     */
    @PatchMapping("{groupId}/mentors/{menteeId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto promoteToMentor(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable("groupId") String groupId,
            @PathVariable("menteeId") String menteeId) {
        String email = userPrincipal.getEmail();
        GroupReturnService groupReturn = groupService.promoteToMentor(email, groupId, menteeId);
        return new ApiResponseDto(
                groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    /**
     * Demotes a mentor to a mentee within a group.
     *
     * @param userPrincipal The current user's principal information.
     * @param groupId       The ID of the group in which the demotion will occur.
     * @param mentorId      The ID of the mentor to be demoted to mentee.
     * @return APIResponse indicating the success or failure of the demotion.
     */
    @PatchMapping("{groupId}/mentees/{mentorId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto demoteToMentee(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable("groupId") String groupId,
            @PathVariable("mentorId") String mentorId) {
        String email = userPrincipal.getEmail();
        GroupReturnService groupReturn = groupService.demoteToMentee(email, groupId, mentorId);
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
        InputStream tempStream = getClass().getResourceAsStream("/templates/temp-import-groups.xlsx");
        File tempFile = new File(TEMP_TEMPLATE_PATH);
        FileUtils.copyInputStreamToFile(tempStream, tempFile);

        InputStream templateStream = getClass().getResourceAsStream("/templates/import-groups.xlsx");
        File templateFile = new File(TEMPLATE_PATH);
        FileUtils.copyInputStreamToFile(templateStream, templateFile);

        Files.copy(templateFile.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        groupService.loadTemplate(tempFile);
        Resource resource = new FileSystemResource(tempFile.getAbsolutePath());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + resource.getFilename())
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .contentLength(resource.getFile().length())
                .body(resource);
    }

    /**
     * Deletes a group.
     *
     * @param userPrincipal The current user's principal information.
     * @param id            The ID of the group to be deleted.
     * @return APIResponse indicating the success or failure of the group deletion.
     */
    @DeleteMapping(value = "{id}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto delete(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal, @PathVariable String id) {
        String email = userPrincipal.getEmail();
        GroupReturnService groupReturn = groupService.deleteGroup(email, id);
        return new ApiResponseDto(
                groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    /**
     * Updates a group's information.
     *
     * @param userPrincipal The current user's principal information.
     * @param id            The ID of the group to be updated.
     * @param request       The request body containing the updated information.
     * @return APIResponse containing the updated Group entity or an error response.
     */
    @PatchMapping("{id}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Group> update(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable String id,
            @RequestBody UpdateGroupRequest request) {
        String email = userPrincipal.getEmail();
        GroupReturnService groupReturn = groupService.updateGroup(email, id, request);
        return new ApiResponseDto(
                groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    /**
     * Retrieves data for the homepage of the mobile app.
     *
     * @param userPrincipal The current user's principal information.
     * @return APIResponse containing data for the homepage.
     */
    @GetMapping("home")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<HomePageResponse> getHomePage(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal) {
        String userId = userPrincipal.getId();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ApiResponseDto.notFound(404);
        }
        List<EventService.Event> events = eventService.getMostRecentEvents(userId);
        List<GroupHomepageResponse> pinnedGroups = groupService.getUserPinnedGroups(userId);
        Slice<GroupHomepageResponse> groups = groupService.getHomePageRecentGroupsOfUser(userId, 0, 25);
        return ApiResponseDto.success(new HomePageResponse(events, pinnedGroups, groups));
    }

    /**
     * Deletes multiple groups.
     *
     * @param userPrincipal The current user's principal information.
     * @param ids           The list of group IDs to be deleted.
     * @return APIResponse indicating the success or failure of the group deletion.
     */
    @DeleteMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto deleteMultiple(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @RequestBody List<String> ids) {
        String email = userPrincipal.getEmail();
        GroupReturnService groupReturn = groupService.deleteMultiple(email, ids);
        return new ApiResponseDto(
                groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    /**
     * Disables multiple groups.
     *
     * @param userPrincipal The current user's principal information.
     * @param ids           The list of group IDs to be disabled.
     * @return APIResponse indicating the success or failure of disabling the groups.
     */
    @PatchMapping(value = "disable")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto disableMultiple(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @RequestBody List<String> ids) {
        String email = userPrincipal.getEmail();
        GroupReturnService groupReturn = groupService.disableMultiple(email, ids);
        return new ApiResponseDto(
                groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    /**
     * Enables multiple groups, checking time start and time end to generate status.
     *
     * @param userPrincipal The current user's principal information.
     * @param ids           The list of group IDs to be enabled.
     * @return APIResponse indicating the success or failure of enabling the groups.
     */
    @PatchMapping(value = "enable")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto enableMultiple(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @RequestBody List<String> ids) {
        String email = userPrincipal.getEmail();
        GroupReturnService groupReturn = groupService.enableMultiple(email, ids);
        return new ApiResponseDto(
                groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    /**
     * Get members of a group for mobile.
     *
     * @param userPrincipal The current user's principal information.
     * @param groupId       The ID of the group for which members are requested.
     * @return APIResponse containing the group members' information.
     */
    @GetMapping("{id}/members")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<GroupMembersResponse> getGroupMembers(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable("id") String groupId) {
        GroupReturnService groupMembers = groupService.getGroupMembers(groupId, userPrincipal.getId());
        return new ApiResponseDto(
                groupMembers.getData(),
                groupMembers.getReturnCode(),
                groupMembers.getMessage());
    }

    /**
     * Pin a group for mobile users.
     *
     * @param userPrincipal The current user's principal information.
     * @param groupId       The ID of the group to be pinned.
     * @return APIResponse indicating the success or failure of the operation.
     */
    @PostMapping("{id}/pin")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Object> pinGroup(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable("id") String groupId) {
        groupService.pinGroup(userPrincipal.getId(), groupId);
        return new ApiResponseDto(true, "OK", 200);
    }

    /**
     * Unpin a group for mobile users.
     *
     * @param userPrincipal The current user's principal information.
     * @param groupId       The ID of the group to be unpinned.
     * @return APIResponse indicating the success or failure of the operation.
     */
    @PostMapping("{id}/unpin")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Object> unpinGroup(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable("id") String groupId) {
        groupService.unpinGroup(userPrincipal.getId(), groupId);
        return new ApiResponseDto(true, "OK", 200);
    }

    /**
     * Get detailed information about a group for mobile users.
     *
     * @param userPrincipal The current user's principal information.
     * @param groupId       The ID of the group for which details are requested.
     * @return APIResponse containing detailed information about the group.
     */
    @GetMapping("{id}/detail")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<GroupDetailResponse> getGroup(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable("id") String groupId) {
        GroupReturnService groupData = groupService.getGroupDetail(userPrincipal.getId(), groupId);
        return new ApiResponseDto(groupData.getData(), groupData.getReturnCode(), groupData.getMessage());
    }

    /**
     * Get media (images and files) of a group for mobile users.
     *
     * @param userPrincipal The current user's principal information.
     * @param groupId       The ID of the group for which media is requested.
     * @return APIResponse containing media information of the group.
     */
    @GetMapping("{id}/media")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<ShortMediaMessage> getGroupMedia(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable("id") String groupId) {
        GroupReturnService groupData = groupService.getGroupMedia(userPrincipal.getId(), groupId);
        return new ApiResponseDto(groupData.getData(), groupData.getReturnCode(), groupData.getMessage());
    }

    /**
     * Update the avatar of a group for mobile users.
     *
     * @param userPrincipal The current user's principal information.
     * @param groupId       The ID of the group for which the avatar is updated.
     * @param file          The multipart file containing the new avatar.
     * @return APIResponse containing the updated avatar information.
     * @throws GeneralSecurityException  If a security exception occurs during the process.
     * @throws IOException               If an I/O exception occurs during the process.
     * @throws ServerException           If a server exception occurs during the process.
     * @throws InsufficientDataException If there is insufficient data for the process.
     * @throws ErrorResponseException    If an error response occurs during the process.
     * @throws InvalidResponseException  If an invalid response occurs during the process.
     * @throws XmlParserException        If an XML parsing exception occurs during the process.
     * @throws InternalException         If an internal exception occurs during the process.
     */
    @PostMapping(value = "{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<String> updateGroupAvatar(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @RequestParam String groupId,
            @RequestParam(value = "file", required = false) MultipartFile file)
            throws GeneralSecurityException, IOException, ServerException, InsufficientDataException, ErrorResponseException, InvalidResponseException, XmlParserException, InternalException {
        GroupReturnService groupData = groupService.updateAvatar(userPrincipal.getId(), groupId, file);
        return new ApiResponseDto(groupData.getData(), groupData.getReturnCode(), groupData.getMessage());
    }

    /**
     * Export the table of groups.
     *
     * @param userPrincipal The current user's principal information.
     * @param remainColumns List of columns to remain in the export.
     * @return ResponseEntity containing the exported group table.
     * @throws IOException If an I/O exception occurs during the export process.
     */
    @GetMapping(value = "export")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Resource> export(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "") List<String> remainColumns)
            throws IOException {
        return groupService.generateExportTable(userPrincipal.getEmail(), remainColumns);
    }

    /**
     * Export the table of mentors in a group.
     *
     * @param userPrincipal The current user's principal information.
     * @param groupId       The ID of the group for which mentors are exported.
     * @param remainColumns List of columns to remain in the export.
     * @return ResponseEntity containing the exported mentors' group table.
     * @throws IOException If an I/O exception occurs during the export process.
     */
    @GetMapping(value = "{groupId}/mentors/export")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Resource> exportMentors(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable String groupId,
            @RequestParam(defaultValue = "") List<String> remainColumns)
            throws IOException {
        return groupService.generateExportTableMembers(userPrincipal.getEmail(), remainColumns, groupId, "MENTOR");
    }

    /**
     * Export the table of mentees in a group.
     *
     * @param userPrincipal The current user's principal information.
     * @param groupId       The ID of the group for which mentees are exported.
     * @param remainColumns List of columns to remain in the export.
     * @return ResponseEntity containing the exported mentees' group table.
     * @throws IOException If an I/O exception occurs during the export process.
     */
    @GetMapping(value = "{groupId}/mentees/export")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Resource> exportMentees(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable String groupId,
            @RequestParam(defaultValue = "") List<String> remainColumns)
            throws IOException {
        return groupService.generateExportTableMembers(userPrincipal.getEmail(), remainColumns, groupId, "MENTEE");
    }

    /**
     * Pin a message for mobile users.
     *
     * @param userPrincipal The current user's principal information.
     * @param groupId       The ID of the group where the message is pinned.
     * @param messageId     The ID of the message to be pinned.
     * @return ResponseEntity indicating the success or failure of the operation.
     */
    @PostMapping("{groupId}/pin-message")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Void> pinMessage(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable String groupId,
            @RequestParam String messageId) {
        boolean isPinned = groupService.pinMessage(userPrincipal.getId(), groupId, messageId);
        if (!isPinned) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Unpin a message for mobile users.
     *
     * @param userPrincipal The current user's principal information.
     * @param groupId       The ID of the group where the message is unpinned.
     * @param messageId     The ID of the message to be unpinned.
     * @return ResponseEntity indicating the success or failure of the operation.
     */
    @PostMapping("{groupId}/unpin-message")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Resource> unpinMessage(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable String groupId,
            @RequestParam String messageId) {
        boolean isPinned = groupService.unpinMessage(userPrincipal.getId(), groupId, messageId);
        if (!isPinned) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Export groups table based on search conditions.
     *
     * @param userPrincipal The current user's principal information.
     * @param name          The name to search for in groups.
     * @param mentorEmail   The email of the mentor to search for in groups.
     * @param menteeEmail   The email of the mentee to search for in groups.
     * @param groupCategory The category of the group to search for.
     * @param timeStart1    The start time of the first time range.
     * @param timeEnd1      The end time of the first time range.
     * @param timeStart2    The start time of the second time range.
     * @param timeEnd2      The end time of the second time range.
     * @param status        The status to search for in groups.
     * @param remainColumns List of columns to remain in the export.
     * @return ResponseEntity containing the exported group table based on search conditions.
     * @throws IOException If an I/O exception occurs during the export process.
     */
    @GetMapping(value = "export/search")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Resource> exportBySearchConditions(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
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
                userPrincipal.getEmail(),
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
     * @param userPrincipal The current user's principal information.
     * @param groupId       The ID of the group for which the workspace is requested.
     * @return ResponseEntity containing the group workspace information.
     */
    @GetMapping("{groupId}/workspace")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<GroupDetailResponse> getWorkspace(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable String groupId) {
        GroupDetailResponse workspace = groupService.getGroupWorkspace(userPrincipal, groupId);
        if (workspace == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(workspace);
    }

    /**
     * Mark a mentee in a group for mobile users.
     *
     * @param userPrincipal The current user's principal information.
     * @param groupId       The ID of the group where the mentee is marked.
     * @param menteeId      The ID of the mentee to be marked.
     * @return ResponseEntity indicating the success or failure of the operation.
     */
    @PostMapping("{groupId}/star")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Void> markMentee(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable String groupId,
            @RequestParam String menteeId) {
        boolean isMarked = groupService.markMentee(userPrincipal, groupId, menteeId);
        if (!isMarked) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Unmark a mentee in a group for mobile users.
     *
     * @param userPrincipal The current user's principal information.
     * @param groupId       The ID of the group where the mentee is unmarked.
     * @param menteeId      The ID of the mentee to be unmarked.
     * @return ResponseEntity indicating the success or failure of the operation.
     */
    @DeleteMapping("{groupId}/star")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Void> unmarkMentee(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable String groupId,
            @RequestParam String menteeId) {
        boolean isMarked = groupService.unmarkMentee(userPrincipal, groupId, menteeId);
        if (!isMarked) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Get the list of group forwards for mobile users.
     *
     * @param userPrincipal The current user's principal information.
     * @param name          Optional name parameter for filtering the list.
     * @return ResponseEntity containing the list of group forwards.
     * @throws ServerException           If a server exception occurs during the process.
     * @throws InsufficientDataException If there is insufficient data for the process.
     * @throws ErrorResponseException    If an error response occurs during the process.
     * @throws IOException               If an I/O exception occurs during the process.
     * @throws NoSuchAlgorithmException  If no such algorithm is found during the process.
     * @throws InvalidKeyException       If an invalid key is encountered during the process.
     * @throws InvalidResponseException  If an invalid response occurs during the process.
     * @throws XmlParserException        If an XML parsing exception occurs during the process.
     * @throws InternalException         If an internal exception occurs during the process.
     */
    @GetMapping("forward")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<List<ChannelForwardResponse>> getListGroupForward(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal, @RequestParam Optional<String> name) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        List<ChannelForwardResponse> listChannelForward = groupService.getGroupForwards(userPrincipal, name);

        return ResponseEntity.ok(listChannelForward);
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
