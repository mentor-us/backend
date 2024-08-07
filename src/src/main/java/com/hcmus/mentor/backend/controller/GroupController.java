package com.hcmus.mentor.backend.controller;

import an.awesome.pipelinr.Pipeline;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
import com.hcmus.mentor.backend.controller.payload.ApiResponseDto;
import com.hcmus.mentor.backend.controller.payload.ReturnCodeConstants;
import com.hcmus.mentor.backend.controller.payload.request.groups.AddMembersRequest;
import com.hcmus.mentor.backend.controller.payload.response.ShortMediaMessage;
import com.hcmus.mentor.backend.controller.payload.response.groups.GroupMembersResponse;
import com.hcmus.mentor.backend.controller.payload.response.groups.UpdateGroupAvatarResponse;
import com.hcmus.mentor.backend.controller.usecase.channel.common.ChannelForwardDto;
import com.hcmus.mentor.backend.controller.usecase.channel.getchannelbyid.GetChannelByIdQuery;
import com.hcmus.mentor.backend.controller.usecase.channel.getchannelforward.GetChannelsForwardQuery;
import com.hcmus.mentor.backend.controller.usecase.channel.getmediabychannelid.GetMediaByChannelIdQuery;
import com.hcmus.mentor.backend.controller.usecase.common.DetailDto;
import com.hcmus.mentor.backend.controller.usecase.group.addmembertogroup.AddMemberToGroupCommand;
import com.hcmus.mentor.backend.controller.usecase.group.common.GroupDetailDto;
import com.hcmus.mentor.backend.controller.usecase.group.common.GroupWorkspaceDto;
import com.hcmus.mentor.backend.controller.usecase.group.creategroup.CreateGroupCommand;
import com.hcmus.mentor.backend.controller.usecase.group.enabledisablestatusgroupbyid.EnableDisableGroupByIdCommand;
import com.hcmus.mentor.backend.controller.usecase.group.getallgroups.GetAllGroupsQuery;
import com.hcmus.mentor.backend.controller.usecase.group.getgroupbyid.GetGroupByIdQuery;
import com.hcmus.mentor.backend.controller.usecase.group.getgroupworkspace.GetGroupWorkSpaceQuery;
import com.hcmus.mentor.backend.controller.usecase.group.gethomepage.GetHomePageQuery;
import com.hcmus.mentor.backend.controller.usecase.group.gethomepage.HomePageDto;
import com.hcmus.mentor.backend.controller.usecase.group.importgroup.ImportGroupCommand;
import com.hcmus.mentor.backend.controller.usecase.group.promotedemoteuser.PromoteDenoteUserByIdCommand;
import com.hcmus.mentor.backend.controller.usecase.group.removemembergroup.RemoveMemberToGroupCommand;
import com.hcmus.mentor.backend.controller.usecase.group.searchowngroups.SearchOwnGroupsQuery;
import com.hcmus.mentor.backend.controller.usecase.group.serachgroups.SearchGroupsQuery;
import com.hcmus.mentor.backend.controller.usecase.group.togglemarkmentee.ToggleMarkMenteeCommand;
import com.hcmus.mentor.backend.controller.usecase.group.updategroupbyid.UpdateGroupByIdCommand;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import com.hcmus.mentor.backend.domain.constant.GroupUserRole;
import com.hcmus.mentor.backend.security.principal.CurrentUser;
import com.hcmus.mentor.backend.security.principal.userdetails.CustomerUserDetails;
import com.hcmus.mentor.backend.service.GroupService;
import com.hcmus.mentor.backend.service.dto.GroupServiceDto;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.hcmus.mentor.backend.controller.payload.ReturnCodeConstants.INVALID_PERMISSION;
import static com.hcmus.mentor.backend.controller.payload.ReturnCodeConstants.USER_NOT_FOUND;

/**
 * Group controller.
 */
@Tag(name = "groups")
@Valid
@RestController
@RequestMapping("api/groups")
@SecurityRequirement(name = "bearer")
@RequiredArgsConstructor
public class GroupController {

    private final Logger logger = LoggerFactory.getLogger(GroupController.class);
    private final GroupService groupService;
    private final Pipeline pipeline;

    /**
     * Retrieves groups based on the user's role and group type.
     * Admins can get all groups (Paging), while users can get mentee groups or mentor groups (Paging).
     *
     * @return APIResponse containing a Page of Group entities based on the specified criteria.
     */
    @GetMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Map<String, Object>> getAllGroup(GetAllGroupsQuery query) {
        var groups = pipeline.send(query);

        return ApiResponseDto.success(pagingResponse(groups));
    }

    /**
     * Retrieves the user's own groups based on the specified type (mentor, mentee, or all).
     *
     * @param query The query containing the user's ID and group type.
     * @return APIResponse containing a Page of GroupHomepageDto entities.
     */
    @GetMapping("own")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Map<String, Object>> getOwnGroups(
            SearchOwnGroupsQuery query) {
        var groups = pipeline.send(query);

        return ApiResponseDto.success(pagingResponse(groups));
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
    public ApiResponseDto<Map<String, Object>> recentGroups(
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
    public ApiResponseDto<GroupDetailDto> getGroupById(
            @PathVariable("id") String id) {
        try {
            var query = GetGroupByIdQuery.builder().id(id).build();

            var group = pipeline.send(query);

            return ApiResponseDto.success(group);
        } catch (DomainException ex) {
            return ApiResponseDto.notFound(USER_NOT_FOUND);
        }
    }

    /**
     * Creates a new group (Only Admins).
     *
     * @param command The request body containing information to create a new group.
     * @return APIResponse containing the created Group entity or an error response.
     */
    @PostMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Group> create(
            @RequestBody CreateGroupCommand command) {
        var groupReturn = pipeline.send(command);

        return new ApiResponseDto(groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    /**
     * Imports multiple groups by a template file.
     *
     * @param file The template file containing group information.
     * @return APIResponse containing a list of imported Group entities or an error response.
     */
    @PostMapping(value = "import", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<List<Group>> importGroups(
            @RequestParam("file") MultipartFile file) {
        var command = ImportGroupCommand.builder().file(file).build();

        GroupServiceDto groupReturn = pipeline.send(command);

        return new ApiResponseDto(groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    /**
     * Finds groups with multiple filters.
     *
     * @return APIResponse containing a Page of Group entities based on the specified criteria.
     */
    @GetMapping("find")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Map<String, Object>> searchGroup(@Valid SearchGroupsQuery query) {
        var groups = pipeline.send(query);

        return ApiResponseDto.success(pagingResponse(groups));
    }

    /**
     * Adds mentees to a group.
     *
     * @param groupId The ID of the group to which mentees will be added.
     * @param request The request body containing mentee information.
     * @return APIResponse containing the updated Group entity or an error response.
     */
    @PostMapping("{groupId}/mentees")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<GroupDetailDto> addMentees(
            @PathVariable("groupId") String groupId,
            @RequestBody AddMembersRequest request) {
        try {
            var command = AddMemberToGroupCommand.builder()
                    .emails(request.getEmails())
                    .groupId(groupId)
                    .isMentor(false)
                    .build();

            var group = pipeline.send(command);

            return ApiResponseDto.success(group);
        } catch (ForbiddenException ex) {
            return new ApiResponseDto<>(null, INVALID_PERMISSION, ex.getMessage());
        } catch (DomainException ex) {
            return new ApiResponseDto<>(null, ReturnCodeConstants.GROUP_USER_NOT_ACTIVATE, ex.getMessage());
        }
    }

    /**
     * Adds mentors to a group.
     *
     * @param groupId The ID of the group to which mentors will be added.
     * @param request The request body containing mentor information.
     * @return APIResponse containing the updated Group entity or an error response.
     */
    @PostMapping("{groupId}/mentors")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<GroupDetailDto> addMentors(
            @PathVariable("groupId") String groupId,
            @RequestBody AddMembersRequest request) {
        try {
            var command = AddMemberToGroupCommand.builder()
                    .emails(request.getEmails())
                    .groupId(groupId)
                    .isMentor(true)
                    .build();

            var group = pipeline.send(command);

            return ApiResponseDto.success(group);
        } catch (ForbiddenException ex) {
            return new ApiResponseDto<>(null, INVALID_PERMISSION, ex.getMessage());
        } catch (DomainException ex) {
            return new ApiResponseDto<>(null, ReturnCodeConstants.GROUP_USER_NOT_ACTIVATE, ex.getMessage());
        }
    }

    /**
     * Deletes a mentee from a group.
     *
     * @param groupId  The ID of the group from which the mentee will be deleted.
     * @param menteeId The ID of the mentee to be deleted from the group.
     * @return APIResponse indicating the success or failure of the operation.
     */
    @DeleteMapping("{groupId}/mentees/{menteeId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto deleteMentee(
            @PathVariable("groupId") String groupId,
            @PathVariable("menteeId") String menteeId) {
        try {
            var command = RemoveMemberToGroupCommand.builder()
                    .userId(menteeId)
                    .groupId(groupId)
                    .build();

            var group = pipeline.send(command);

            return ApiResponseDto.success(group);
        } catch (ForbiddenException ex) {
            return new ApiResponseDto<>(null, INVALID_PERMISSION, ex.getMessage());
        } catch (DomainException ex) {
            return new ApiResponseDto<>(null, USER_NOT_FOUND, ex.getMessage());
        }
    }

    /**
     * Deletes a mentor from a group.
     *
     * @param groupId  The ID of the group from which the mentor will be deleted.
     * @param mentorId The ID of the mentor to be deleted from the group.
     * @return APIResponse indicating the success or failure of the operation.
     */
    @DeleteMapping("{groupId}/mentors/{mentorId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto deleteMentor(
            @PathVariable("groupId") String groupId,
            @PathVariable("mentorId") String mentorId) {
        try {
            var command = RemoveMemberToGroupCommand.builder()
                    .userId(mentorId)
                    .groupId(groupId)
                    .build();

            var group = pipeline.send(command);

            return ApiResponseDto.success(group);
        } catch (ForbiddenException ex) {
            return new ApiResponseDto<>(null, INVALID_PERMISSION, ex.getMessage());
        } catch (DomainException ex) {
            return new ApiResponseDto<>(null, USER_NOT_FOUND, ex.getMessage());
        }
    }

    /**
     * Promotes a mentee to a mentor within a group.
     *
     * @param groupId  The ID of the group in which the promotion will occur.
     * @param menteeId The ID of the mentee to be promoted to mentor.
     * @return APIResponse indicating the success or failure of the promotion.
     */
    @PatchMapping("{groupId}/mentors/{menteeId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto promoteToMentor(
            @PathVariable("groupId") String groupId,
            @PathVariable("menteeId") String menteeId) {
        try {
            var command = PromoteDenoteUserByIdCommand.builder()
                    .userId(menteeId)
                    .groupId(groupId)
                    .toMentor(true)
                    .build();

            var group = pipeline.send(command);

            return ApiResponseDto.success(group);
        } catch (ForbiddenException ex) {
            return new ApiResponseDto<>(null, INVALID_PERMISSION, ex.getMessage());
        } catch (DomainException ex) {
            return new ApiResponseDto<>(null, USER_NOT_FOUND, ex.getMessage());
        }
    }

    /**
     * Demotes a mentor to a mentee within a group.
     *
     * @param groupId  The ID of the group in which the demotion will occur.
     * @param mentorId The ID of the mentor to be demoted to mentee.
     * @return APIResponse indicating the success or failure of the demotion.
     */
    @PatchMapping("{groupId}/mentees/{mentorId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto demoteToMentee(
            @PathVariable("groupId") String groupId,
            @PathVariable("mentorId") String mentorId) {
        try {
            var command = PromoteDenoteUserByIdCommand.builder()
                    .userId(mentorId)
                    .groupId(groupId)
                    .toMentor(false)
                    .build();

            var group = pipeline.send(command);

            return ApiResponseDto.success(group);
        } catch (ForbiddenException ex) {
            return new ApiResponseDto<>(null, INVALID_PERMISSION, ex.getMessage());
        } catch (DomainException ex) {
            return new ApiResponseDto<>(null, USER_NOT_FOUND, ex.getMessage());
        }
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
     * @param command The request body containing the updated information.
     * @return APIResponse containing the updated Group entity or an error response.
     */
    @PatchMapping("{id}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<GroupDetailDto> update(
            @PathVariable String id,
            @RequestBody UpdateGroupByIdCommand command) {
        command.setId(id);

        try {
            var group = pipeline.send(command);

            return ApiResponseDto.success(group);
        } catch (DomainException ex) {
            return new ApiResponseDto(null, Integer.parseInt(ex.getCode()), ex.getMessage());
        }
    }

    /**
     * Retrieves data for the homepage of the mobile app.
     *
     * @return APIResponse containing data for the homepage.
     */
    @GetMapping("home")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<HomePageDto> getHomePage() {
        var query = GetHomePageQuery.builder().build();
        var response = pipeline.send(query);

        return ApiResponseDto.success(response);
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
     * @param ids The list of group IDs to be disabled.
     * @return APIResponse indicating the success or failure of disabling the groups.
     */
    @PatchMapping(value = "disable")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto disableMultiple(
            @RequestBody List<String> ids) {
        var groups = new ArrayList<GroupDetailDto>();
        var notfoundGroups = new ArrayList<String>();

        for (var groupId : ids) {
            try {
                var comamnd = EnableDisableGroupByIdCommand.builder().id(groupId).status(GroupStatus.DISABLED).build();

                var group = pipeline.send(comamnd);

                groups.add(group);
            } catch (ForbiddenException ex) {
                return new ApiResponseDto(null, INVALID_PERMISSION, "Không có quyền chỉnh sửa");
            } catch (DomainException ex) {
                notfoundGroups.add(groupId);
            }
        }
        if (!notfoundGroups.isEmpty()) {
            return new ApiResponseDto(notfoundGroups, USER_NOT_FOUND, "Không tìm thấy nhóm");
        }

        return new ApiResponseDto(groups, 200, "Success");
    }

    /**
     * Enables multiple groups, checking time start and time end to generate status.
     *
     * @param ids The list of group IDs to be enabled.
     * @return APIResponse indicating the success or failure of enabling the groups.
     */
    @PatchMapping(value = "enable")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto enableMultiple(
            @RequestBody List<String> ids) {
        var groups = new ArrayList<GroupDetailDto>();
        var notfoundGroups = new ArrayList<String>();

        for (var groupId : ids) {
            try {
                var comamnd = EnableDisableGroupByIdCommand.builder().id(groupId).status(GroupStatus.ACTIVE).build();

                var group = pipeline.send(comamnd);

                groups.add(group);
            } catch (ForbiddenException ex) {
                return new ApiResponseDto(null, INVALID_PERMISSION, "Không có quyền chỉnh sửa");
            } catch (DomainException ex) {
                notfoundGroups.add(groupId);
            }
        }
        if (!notfoundGroups.isEmpty()) {
            return new ApiResponseDto(notfoundGroups, USER_NOT_FOUND, "Không tìm thấy nhóm");
        }

        return new ApiResponseDto(groups, 200, "Success");
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
     * (Use /api/channels/{id} for channel) (/api/groups/{id} for group) Get detailed information about a channel (if not found it will get the group for compatibility issue).
     *
     * @param id The ID of the group for which details are requested.
     * @return APIResponse containing detailed information about the group.
     */
    @Deprecated(forRemoval = true)
    @GetMapping("{id}/detail")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<DetailDto> getChannelById(
            @PathVariable("id") String id) {
        try {
            var query = GetChannelByIdQuery.builder()
                    .id(id)
                    .build();

            var channel = pipeline.send(query);

            return ApiResponseDto.success(channel);
        } catch (ForbiddenException ex) {
            return new ApiResponseDto<>(null, INVALID_PERMISSION, ex.getMessage());
        } catch (DomainException ex) {
            if (ex.getMessage() == "Không tìm thấy kênh") {
                try {
                    var query = GetGroupByIdQuery.builder().id(id).isDetail(true).build();

                    var group = pipeline.send(query);

                    return ApiResponseDto.success(group);
                } catch (DomainException groupException) {
                    return new ApiResponseDto<>(null, USER_NOT_FOUND, groupException.getMessage());
                }
            }

            return new ApiResponseDto<>(null, USER_NOT_FOUND, ex.getMessage());
        }
    }

    /**
     * (Use /api/channels/{id}/media) Get media (images and files) of a group for mobile users.
     *
     * @param id The ID of the group for which media is requested.
     * @return APIResponse containing media information of the group.
     */
    @Deprecated(forRemoval = true)
    @GetMapping("{id}/media")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<List<ShortMediaMessage>> getGroupMedia(
            @PathVariable("id") String id) {
        try {
            var query = GetMediaByChannelIdQuery.builder()
                    .id(id)
                    .build();

            var medias = pipeline.send(query);

            return ApiResponseDto.success(medias);
        } catch (ForbiddenException ex) {
            return new ApiResponseDto<>(null, INVALID_PERMISSION, ex.getMessage());
        }
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
        return groupService.generateExportTableMembers(customerUserDetails.getEmail(), remainColumns, groupId, GroupUserRole.MENTOR);
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
        return groupService.generateExportTableMembers(customerUserDetails.getEmail(), remainColumns, groupId, GroupUserRole.MENTEE);
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
     * @param groupId The ID of the group for which the workspace is requested.
     * @return ResponseEntity containing the group workspace information.
     */
    @GetMapping("{groupId}/workspace")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<GroupWorkspaceDto> getWorkspace(
            @PathVariable String groupId) {
        var query = GetGroupWorkSpaceQuery.builder()
                .groupId(groupId)
                .build();

        var response = pipeline.send(query);

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
     * @param name Optional name parameter for filtering the list.
     * @return ResponseEntity containing the list of group forwards.
     */
    @GetMapping("forward")
    @SneakyThrows
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<List<ChannelForwardDto>> getListGroupForward(
            @RequestParam Optional<String> name) {
        var query = new GetChannelsForwardQuery(name.orElse(""));

        return ResponseEntity.ok(pipeline.send(query));
    }

    private Map<String, Object> pagingResponse(Page<?> groups) {
        return Map.ofEntries(
                Map.entry("groups", groups.getContent()),
                Map.entry("currentPage", groups.getNumber()),
                Map.entry("totalItems", groups.getTotalElements()),
                Map.entry("totalPages", groups.getTotalPages())
        );
    }
}