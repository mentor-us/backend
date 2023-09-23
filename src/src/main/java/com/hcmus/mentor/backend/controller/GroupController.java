package com.hcmus.mentor.backend.controller;

import com.hcmus.mentor.backend.entity.Group;
import com.hcmus.mentor.backend.entity.User;
import com.hcmus.mentor.backend.payload.APIResponse;
import com.hcmus.mentor.backend.payload.request.groups.AddMenteesRequest;
import com.hcmus.mentor.backend.payload.request.groups.AddMentorsRequest;
import com.hcmus.mentor.backend.payload.request.groups.CreateGroupRequest;
import com.hcmus.mentor.backend.payload.request.groups.UpdateGroupRequest;
import com.hcmus.mentor.backend.payload.response.HomePageResponse;
import com.hcmus.mentor.backend.payload.response.ShortMediaMessage;
import com.hcmus.mentor.backend.payload.response.groups.GroupDetailResponse;
import com.hcmus.mentor.backend.payload.response.groups.GroupHomepageResponse;
import com.hcmus.mentor.backend.payload.response.groups.GroupMembersResponse;
import com.hcmus.mentor.backend.payload.returnCode.GroupReturnCode;
import com.hcmus.mentor.backend.repository.GroupCategoryRepository;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.CurrentUser;
import com.hcmus.mentor.backend.security.UserPrincipal;
import com.hcmus.mentor.backend.service.*;
import com.hcmus.mentor.backend.service.GroupService.GroupReturnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.util.*;

import static com.hcmus.mentor.backend.payload.returnCode.UserReturnCode.NOT_FOUND;

@Tag(name = "Group APIs", description = "REST APIs for Group collections")
@RestController
@RequestMapping("/api/groups")
public class GroupController {
    private final static Logger LOGGER = LogManager.getLogger(FileController.class);
    private final GroupRepository groupRepository;

    private final UserRepository userRepository;

    private final UserService userService;

    private final GroupService groupService;
    private final MeetingService meetingService;
    private final TaskService taskService;
    private final GroupCategoryRepository groupCategoryRepository;
    private final EventService eventService;
    private final PermissionService permissionService;
    private final String TEMPLATE_PATH = "src/main/resources/templates/import-groups.xlsx";
    private final String TEMP_TEMPLATE_PATH = "src/main/resources/templates/temp-import-groups.xlsx";

    public GroupController(GroupRepository groupRepository,
                           UserRepository userRepository,
                           UserService userService,
                           GroupService groupService, MeetingService meetingService, TaskService taskService, GroupCategoryRepository groupCategoryRepository, EventService eventService, PermissionService permissionService) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.groupService = groupService;
        this.meetingService = meetingService;
        this.taskService = taskService;
        this.groupCategoryRepository = groupCategoryRepository;
        this.eventService = eventService;
        this.permissionService = permissionService;
    }

    @Operation(summary = "Get groups (Paging)",
            description = "Get groups based on your role and group type you want\n" +
                    "Admin: Get all groups (Paging)\n" +
                    "User: Get mentee groups or mentor groups (Paging)", tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            )})
    @GetMapping(value = {"/", ""})
    public APIResponse<Page<Group>> all(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "25") int pageSize,
                           @RequestParam(defaultValue = "") String type) {
        Page<Group> groups = new PageImpl<>(new ArrayList<>());
        if (!type.equals("admin")) {
            return APIResponse.success(pagingResponse(groups));
        }

        boolean isSuperAdmin = permissionService.isSuperAdmin(userPrincipal.getEmail());
        Pageable pageRequest = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate"));
        if (isSuperAdmin) {
            groups = groupRepository.findAll(pageRequest);
        }
        else{
            String creatorId = userRepository.findByEmail(userPrincipal.getEmail()).get().getId();
            groups = groupRepository.findAllByCreatorId(pageRequest, creatorId);
        }
        for (Group group : groups) {
            if(group.getStatus() != Group.Status.DELETED && group.getStatus() != Group.Status.DISABLED){
                if (group.getTimeEnd().before(new Date())) {
                    group.setStatus(Group.Status.OUTDATED);
                    groupRepository.save(group);
                }
                if (group.getTimeStart().after(new Date())) {
                    group.setStatus(Group.Status.INACTIVE);
                    groupRepository.save(group);
                }
            }
        }
        return APIResponse.success(pagingResponse(groups));
    }

    private Map<String, Object> pagingResponse(Page<Group> groups) {
        Map<String, Object> response = new HashMap<>();
        response.put("groups", groups.getContent());
        response.put("currentPage", groups.getNumber());
        response.put("totalItems", groups.getTotalElements());
        response.put("totalPages", groups.getTotalPages());
        return response;
    }

    @Operation(summary = "Get own groups (Paging)",
            description = "Get your own groups based on type param\n" +
                    "mentor: Get all groups as you do mentor (Paging)\n" +
                    "mentee: Get all groups as you do mentee (Paging)", tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            )})
    @GetMapping("/own")
    public APIResponse<Page<GroupHomepageResponse>> getOwnGroups(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "25") int pageSize,
                                        @RequestParam(defaultValue = "") String type) {
        Page<GroupHomepageResponse> groups;
        switch (type) {
            case "mentor":
                groups = groupService.findMentorGroups(userPrincipal.getId(),
                        page, pageSize);
                break;
            case "mentee":
                groups = groupService.findMenteeGroups(userPrincipal.getId(),
                        page, pageSize);
                break;
            default:
                groups = groupService.findOwnGroups(userPrincipal.getId(), page, pageSize);
                break;
        }
        User user = userRepository.findById(userPrincipal.getId()).get();
        for(GroupHomepageResponse group: groups){
            Boolean isPinned = user.isPinnedGroup(group.getId());
            group.setPinned(isPinned);
        }
        return APIResponse.success(pagingHomepageResponse(groups));
    }

    private Map<String, Object> pagingHomepageResponse(Page<GroupHomepageResponse> groups) {
        Map<String, Object> response = new HashMap<>();
        response.put("groups", groups.getContent());
        response.put("currentPage", groups.getNumber());
        response.put("totalItems", groups.getTotalElements());
        response.put("totalPages", groups.getTotalPages());
        return response;
    }

    @Operation(summary = "Get recent groups (Paging)", description = "Get recent updated groups of any user", tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            )})
    @GetMapping("/recent")
    public APIResponse<Page<Group>> recentGroups(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "25") int pageSize) {
        Page<Group> groups = groupService.findRecentGroupsOfUser(userPrincipal.getId(), page, pageSize);
        return APIResponse.success(pagingResponse(groups));
    }

    @Operation(summary = "Get existing group by group id", description = "Get existing group information by groupp id", tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            )})
    @GetMapping("/{id}")
    public APIResponse<Group> get(@PathVariable("id") String id) {
        Optional<Group> groupWrapper = groupRepository.findById(id);
        return groupWrapper.map(APIResponse::success).orElseGet(()-> APIResponse.notFound(NOT_FOUND));
    }

    @Operation(summary = "Create new group", description = "Create new group (Only Admin)", tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retrieve successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication"),
            @ApiResponse(responseCode = GroupReturnCode.DUPLICATE_GROUP_STRING, description = "Group name has been duplicated"),
            @ApiResponse(responseCode = GroupReturnCode.GROUP_CATEGORY_NOT_FOUND_STRING, description = "Group category not exists"),
            @ApiResponse(responseCode = GroupReturnCode.TIME_END_BEFORE_TIME_START_STRING, description = "Time end can't be before time start"),
            @ApiResponse(responseCode = GroupReturnCode.TIME_END_BEFORE_NOW_STRING, description = "Time end can't be before now"),
            @ApiResponse(responseCode = GroupReturnCode.TIME_END_TOO_FAR_FROM_TIME_START_STRING, description = "Time end is too far from time start"),
            @ApiResponse(responseCode = GroupReturnCode.TIME_START_TOO_FAR_FROM_NOW_STRING, description = "Time start is too far from now"),
            @ApiResponse(responseCode = GroupReturnCode.INVALID_EMAILS_STRING, description = "Invalid emails"),
            @ApiResponse(responseCode = GroupReturnCode.INVALID_DOMAINS_STRING, description = "Invalid domains"),
            @ApiResponse(responseCode = GroupReturnCode.DUPLICATE_EMAIL_STRING, description = "Duplicate email"),
    })
    @PostMapping(value = {"", "/"})
    public APIResponse<Group> create(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                              @RequestBody CreateGroupRequest request) {
        String email = userPrincipal.getEmail();
        GroupReturnService groupReturn = groupService.createNewGroup(email, request);
        return new APIResponse(groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    @Operation(summary = "Import multiple groups", description = "Import multiple groups by template file", tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Import successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = GroupReturnCode.INVALID_TEMPLATE_STRING, description = "Invalid Template"),
            @ApiResponse(responseCode = GroupReturnCode.DUPLICATE_GROUP_STRING, description = "Group name has been duplicated"),
            @ApiResponse(responseCode = GroupReturnCode.GROUP_CATEGORY_NOT_FOUND_STRING, description = "Group category not exists"),
            @ApiResponse(responseCode = GroupReturnCode.TIME_END_BEFORE_TIME_START_STRING, description = "Time end can't be before time start"),
            @ApiResponse(responseCode = GroupReturnCode.TIME_END_BEFORE_NOW_STRING, description = "Time end can't be before now"),
            @ApiResponse(responseCode = GroupReturnCode.TIME_END_TOO_FAR_FROM_TIME_START_STRING, description = "Time end is too far from time start"),
            @ApiResponse(responseCode = GroupReturnCode.TIME_START_TOO_FAR_FROM_NOW_STRING, description = "Time start is too far from now"),
            @ApiResponse(responseCode = GroupReturnCode.INVALID_EMAILS_STRING, description = "Invalid emails"),
            @ApiResponse(responseCode = GroupReturnCode.INVALID_DOMAINS_STRING, description = "Invalid domains"),
            @ApiResponse(responseCode = GroupReturnCode.DUPLICATE_EMAIL_STRING, description = "Duplicate email"),
            @ApiResponse(responseCode = GroupReturnCode.NOT_FOUND_STRING, description = "Not found group name"),
    })
    @PostMapping(value = "/import", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public APIResponse<List<Group>> importGroups(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                    @RequestParam("file") MultipartFile file) throws IOException {
        String email = userPrincipal.getEmail();
        GroupReturnService groupReturn = groupService.importGroups(email, file);
        return new APIResponse(groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    @Operation(summary = "Find groups", description = "Find groups with multiple filters", tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retrieve successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            )
    })
    @GetMapping("/find")
    public APIResponse<Page<Group>> get(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                           @RequestParam(defaultValue = "") String name,
                           @RequestParam(defaultValue = "") String mentorEmail,
                           @RequestParam(defaultValue = "") String menteeEmail,
                           @RequestParam(defaultValue = "") String groupCategory,
                           @RequestParam(defaultValue = "1970-01-01T00:00:00")
                               @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date timeStart1,
                           @RequestParam(defaultValue = "2300-01-01T00:00:00")
                               @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date timeEnd1,
                           @RequestParam(defaultValue = "1970-01-01T00:00:00")
                               @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date timeStart2,
                           @RequestParam(defaultValue = "2300-01-01T00:00:00")
                               @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date timeEnd2,
                           @RequestParam(required = false) String status,
                           @RequestParam(defaultValue = "0") Integer page,
                           @RequestParam(defaultValue = "25") Integer size) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        String email = userPrincipal.getEmail();
        GroupReturnService groupReturn = groupService.findGroups(email, name, mentorEmail, menteeEmail, groupCategory, timeStart1, timeEnd1, timeStart2, timeEnd2, status, page, size);
        return new APIResponse(pagingResponse((Page<Group>) groupReturn.getData()), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    @Operation(summary = "Add mentees to group", description = "", tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retrieve successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication"),
            @ApiResponse(responseCode = GroupReturnCode.NOT_FOUND_STRING, description = "Not found group"),
    })
    @PostMapping("/{groupId}/mentees")
    public APIResponse<Group> addMentees(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                  @PathVariable("groupId") String groupId,
                                  @RequestBody AddMenteesRequest request) {
        String email = userPrincipal.getEmail();
        GroupReturnService groupReturn = groupService.addMentees(email, groupId, request);
        return new APIResponse(groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    @Operation(summary = "Add mentors to group", description = "", tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retrieve successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication"),
            @ApiResponse(responseCode = GroupReturnCode.NOT_FOUND_STRING, description = "Not found group"),
    })
    @PostMapping("/{groupId}/mentors")
    public APIResponse<Group> addMentors(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                  @PathVariable("groupId") String groupId,
                                  @RequestBody AddMentorsRequest request) {
        String email = userPrincipal.getEmail();
        GroupReturnService groupReturn = groupService.addMentors(email, groupId, request);
        return new APIResponse(groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }
    @Operation(summary = "delete mentee from group", description = "", tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retrieve successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication"),
            @ApiResponse(responseCode = GroupReturnCode.NOT_FOUND_STRING, description = "Not found group"),
            @ApiResponse(responseCode = GroupReturnCode.MENTEE_NOT_FOUND_STRING, description = "Not found mentee"),
    })
    @DeleteMapping("/{groupId}/mentees/{menteeId}")
    public APIResponse deleteMentee(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                    @PathVariable("groupId") String groupId,
                                    @PathVariable("menteeId") String menteeId) {
        String email = userPrincipal.getEmail();
        GroupReturnService groupReturn = groupService.deleteMentee(email, groupId, menteeId);
        return new APIResponse(groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    @Operation(summary = "delete mentor from group", description = "", tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retrieve successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication"),
            @ApiResponse(responseCode = GroupReturnCode.NOT_FOUND_STRING, description = "Not found group"),
            @ApiResponse(responseCode = GroupReturnCode.MENTOR_NOT_FOUND_STRING, description = "Not found mentor"),
    })
    @DeleteMapping("/{groupId}/mentors/{mentorId}")
    public APIResponse deleteMentor(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                    @PathVariable("groupId") String groupId,
                                    @PathVariable("mentorId") String mentorId) {
        String email = userPrincipal.getEmail();
        GroupReturnService groupReturn = groupService.deleteMentor(email, groupId, mentorId);
        return new APIResponse(groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }
    @Operation(summary = "Promote a mentee to mentor in group", description = "", tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retrieve successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication"),
            @ApiResponse(responseCode = GroupReturnCode.NOT_FOUND_STRING, description = "Not found group"),
            @ApiResponse(responseCode = GroupReturnCode.MENTEE_NOT_FOUND_STRING, description = "Not found mentor"),
    })
    @PatchMapping("/{groupId}/mentors/{menteeId}")
    public APIResponse promoteToMentor(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                       @PathVariable("groupId") String groupId,
                                       @PathVariable("menteeId") String menteeId) {
        String email = userPrincipal.getEmail();
        GroupReturnService groupReturn = groupService.promoteToMentor(email, groupId, menteeId);
        return new APIResponse(groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }
    @Operation(summary = "Demote a mentor to mentee in group", description = "", tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retrieve successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication"),
            @ApiResponse(responseCode = GroupReturnCode.NOT_FOUND_STRING, description = "Not found group"),
            @ApiResponse(responseCode = GroupReturnCode.MENTOR_NOT_FOUND_STRING, description = "Not found mentor"),
    })
    @PatchMapping("/{groupId}/mentees/{mentorId}")
    public APIResponse demoteToMentee(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                      @PathVariable("groupId") String groupId,
                                      @PathVariable("mentorId") String mentorId) {
        String email = userPrincipal.getEmail();
        GroupReturnService groupReturn = groupService.demoteToMentee(email, groupId, mentorId);
        return new APIResponse(groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    @Operation(summary = "Get template import file", description = "", tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class)))
            )})
    @GetMapping("/import")
    public ResponseEntity<Resource> getTemplate() throws Exception {
        InputStream tempStream = getClass().getResourceAsStream("/templates/temp-import-groups.xlsx");
        File tempFile = new File(TEMP_TEMPLATE_PATH);
        FileUtils.copyInputStreamToFile(tempStream, tempFile);

        InputStream templateStream = getClass().getResourceAsStream("/templates/import-groups.xlsx");
        File templateFile = new File(TEMPLATE_PATH);
        FileUtils.copyInputStreamToFile(templateStream, templateFile);

//        if (!tempFile.exists()) {
//            tempFile = ResourceUtils.getFile("classpath:templates/temp-import-groups.xlsx");
//        }
//        File templateFile = new File(TEMPLATE_PATH);
//        if (!templateFile.exists()) {
//            templateFile = ResourceUtils.getFile("classpath:templates/import-groups.xlsx");
//        }
        Files.copy(templateFile.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        groupService.loadTemplate(tempFile);
        Resource resource = new FileSystemResource(tempFile.getAbsolutePath());
        ResponseEntity<Resource> response = ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + resource.getFilename())
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .contentLength(resource.getFile().length())
                .body(resource);
        //resource.getFile().delete();
        return response;
    }

    public File getResourceAsFile(String resourcePath) {
        try {
            InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(resourcePath);
            if (in == null) {
                return null;
            }

            File tempFile = File.createTempFile(String.valueOf(in.hashCode()), ".tmp");
            tempFile.deleteOnExit();

            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                //copy stream
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Operation(summary = "Delete a group", description = "", tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retrieve successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication"),
            @ApiResponse(responseCode = GroupReturnCode.NOT_FOUND_STRING, description = "Not found group"),
    })
    @DeleteMapping(value = "/{id}")
    public APIResponse delete(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                              @PathVariable String id) {
        String email = userPrincipal.getEmail();
        GroupReturnService groupReturn = groupService.deleteGroup(email, id);
        return new APIResponse(groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    @Operation(summary = "Update a group", description = "", tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retrieve successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication"),
            @ApiResponse(responseCode = GroupReturnCode.NOT_FOUND_STRING, description = "Not found group"),
            @ApiResponse(responseCode = GroupReturnCode.DUPLICATE_GROUP_STRING, description = "Group name has been duplicated"),
            @ApiResponse(responseCode = GroupReturnCode.GROUP_CATEGORY_NOT_FOUND_STRING, description = "Group category not exists"),
            @ApiResponse(responseCode = GroupReturnCode.TIME_END_BEFORE_TIME_START_STRING, description = "Time end can't be before time start"),
    })
    @PatchMapping("/{id}")
    public APIResponse<Group> update(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                              @PathVariable String id,
                              @RequestBody UpdateGroupRequest request) {
        String email = userPrincipal.getEmail();
        GroupReturnService groupReturn = groupService.updateGroup(email, id, request);
        return new APIResponse(groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    @Operation(summary = "Data for Homepage mobile", description = "Contains data for homepage of mobile app, contains: Recent tasks, meetings, pinned groups and recent groups", tags = "Homepage Mobile APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retrieve successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @GetMapping("/home")
    public APIResponse<HomePageResponse> getHomePage(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal) {
        String userId = userPrincipal.getId();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return APIResponse.notFound(404);
        }
        List<EventService.Event> events = eventService.getMostRecentEvents(userId);
        List<GroupHomepageResponse> pinnedGroups = groupService.getUserPinnedGroups(userId);
        Slice<GroupHomepageResponse> groups = groupService.getHomePageRecentGroupsOfUser(userId, 0, 25);
        return APIResponse.success(new HomePageResponse(events, pinnedGroups, groups));
    }

    @Operation(summary = "Delete multiple groups", description = "", tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Delete successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication"),
            @ApiResponse(responseCode = GroupReturnCode.NOT_FOUND_STRING, description = "Not found group"),
    })
    @DeleteMapping(value = {"/", ""})
    public APIResponse deleteMultiple(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                              @RequestBody List<String> ids) {
        String email = userPrincipal.getEmail();
        GroupReturnService groupReturn = groupService.deleteMultiple(email, ids);
        return new APIResponse(groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    @Operation(summary = "Disable multiple groups", description = "", tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disable successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication"),
            @ApiResponse(responseCode = GroupReturnCode.NOT_FOUND_STRING, description = "Not found group"),
    })
    @PatchMapping(value = "/disable")
    public APIResponse disableMultiple(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                      @RequestBody List<String> ids) {
        String email = userPrincipal.getEmail();
        GroupReturnService groupReturn = groupService.disableMultiple(email, ids);
        return new APIResponse(groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }
    @Operation(summary = "Enable multiple groups(BE checks time start and time end to generate status)", description = "", tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Enable successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication"),
            @ApiResponse(responseCode = GroupReturnCode.NOT_FOUND_STRING, description = "Not found group"),
    })
    @PatchMapping(value = "/enable")
    public APIResponse enableMultiple(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                       @RequestBody List<String> ids) {
        String email = userPrincipal.getEmail();
        GroupReturnService groupReturn = groupService.enableMultiple(email, ids);
        return new APIResponse(groupReturn.getData(), groupReturn.getReturnCode(), groupReturn.getMessage());
    }

    @Operation(summary = "Get members of group (Mobile)", description = "Get all members(mentor, mentee) of a group", tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retrieve successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @GetMapping("/{id}/members")
    public APIResponse<GroupMembersResponse> getGroupMembers(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                       @PathVariable("id") String groupId) {
        GroupReturnService groupMembers = groupService.getGroupMembers(groupId, userPrincipal.getId());
        return new APIResponse((GroupMembersResponse)groupMembers.getData(),
                groupMembers.getReturnCode(), groupMembers.getMessage());
    }

    @Operation(summary = "Pin a group (Mobile)", description = "Member of group can pin this one on their homepage", tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pin successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @PostMapping("/{id}/pin")
    public APIResponse<Object> pinGroup(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                @PathVariable("id") String groupId) {
        groupService.pinGroup(userPrincipal.getId(), groupId);
        return new APIResponse(true, "OK", 200);
    }

    @Operation(summary = "Unpin a group (Mobile)", description = "Member of group can unpin this one on their homepage", tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unpin successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @PostMapping("/{id}/unpin")
    public APIResponse<Object> unpinGroup(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                        @PathVariable("id") String groupId) {
        groupService.unpinGroup(userPrincipal.getId(), groupId);
        return new APIResponse(true, "OK", 200);
    }

    @Operation(summary = "Get group detail (Mobile)", description = "Get detail infomation of a group", tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retrieve successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @GetMapping("/{id}/detail")
    public APIResponse<GroupDetailResponse> getGroup(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                                       @PathVariable("id") String groupId) {
        GroupReturnService groupData = groupService.getGroupDetail(userPrincipal.getId(), groupId);
        return new APIResponse(groupData.getData(), groupData.getReturnCode(), groupData.getMessage());
    }

    @Operation(summary = "Get group media (Media collection)",
            description = "Get all images and files uploaded on group", tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retrieve successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @GetMapping("/{id}/media")
    public APIResponse<ShortMediaMessage> getGroupMedia(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                                     @PathVariable("id") String groupId) {
        GroupReturnService groupData = groupService.getGroupMedia(userPrincipal.getId(), groupId);
        return new APIResponse(groupData.getData(), groupData.getReturnCode(), groupData.getMessage());
    }

    @Operation(summary = "Update group avatar (Mobile)",
            description = "Update avatar of group", tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Update successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public APIResponse<String> updateGroupAvatar(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                                        @RequestParam String groupId,
                                                        @RequestParam(value = "file", required = false) MultipartFile file) throws GeneralSecurityException, IOException {
        GroupReturnService groupData = groupService.updateAvatar(userPrincipal.getId(), groupId, file);
        return new APIResponse(groupData.getData(), groupData.getReturnCode(), groupData.getMessage());
    }

    @Operation(summary = "Export group table",
            description = "Export group table", tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Update successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @GetMapping(value = "/export")
    public ResponseEntity<Resource> export(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                           @RequestParam(defaultValue = "") List<String> remainColumns) throws IOException {
        ResponseEntity<Resource> response = groupService.generateExportTable(userPrincipal.getEmail(),remainColumns);
        return response;
    }

    @Operation(summary = "Export mentor's group table",
            description = "Export mentor's group table", tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Update successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @GetMapping(value = "/{groupId}/mentors/export")
    public ResponseEntity<Resource> exportMentors(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                           @PathVariable String groupId,
                                           @RequestParam(defaultValue = "") List<String> remainColumns) throws IOException {
        ResponseEntity<Resource> response = groupService.generateExportTableMembers(userPrincipal.getEmail(),remainColumns, groupId, "MENTOR");
        return response;
    }

    @Operation(summary = "Export mentee's group table",
            description = "Export mentee's group table", tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Update successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @GetMapping(value = "/{groupId}/mentees/export")
    public ResponseEntity<Resource> exportMentees(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                                  @PathVariable String groupId,
                                                  @RequestParam(defaultValue = "") List<String> remainColumns) throws IOException {
        ResponseEntity<Resource> response = groupService.generateExportTableMembers(userPrincipal.getEmail(),remainColumns, groupId, "MENTEE");
        return response;
    }

    @Operation(summary = "Pin message (Mobile)",
            description = "Pin message on top of group", tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pin successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @PostMapping("/{groupId}/pin-message")
    public ResponseEntity<Void> pinMessage(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                           @PathVariable String groupId,
                                           @RequestParam String messageId) {
        boolean isPinned = groupService.pinMessage(userPrincipal.getId(), groupId, messageId);
        if (!isPinned) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Unpin message (Mobile)",
            description = "Unpin message on top of group", tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pin successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @PostMapping("/{groupId}/unpin-message")
    public ResponseEntity<Resource> unpinMessage(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                                  @PathVariable String groupId,
                                                  @RequestParam String messageId) {
        boolean isPinned = groupService.unpinMessage(userPrincipal.getId(), groupId, messageId);
        if (!isPinned) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Export groups table by search conditions",
            description = "Export groups table", tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Update successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @GetMapping(value = "/export/search")
    public ResponseEntity<Resource> exportBySearchConditions(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                                             @RequestParam(defaultValue = "") String name,
                                                             @RequestParam(defaultValue = "") String mentorEmail,
                                                             @RequestParam(defaultValue = "") String menteeEmail,
                                                             @RequestParam(defaultValue = "") String groupCategory,
                                                             @RequestParam(defaultValue = "1970-01-01T00:00:00")
                                                                 @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date timeStart1,
                                                             @RequestParam(defaultValue = "2300-01-01T00:00:00")
                                                                 @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date timeEnd1,
                                                             @RequestParam(defaultValue = "1970-01-01T00:00:00")
                                                                 @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date timeStart2,
                                                             @RequestParam(defaultValue = "2300-01-01T00:00:00")
                                                                 @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date timeEnd2,
                                                             @RequestParam(required = false) String status,
                                                             @RequestParam(defaultValue = "") List<String> remainColumns) throws IOException {
        ResponseEntity<Resource> response = groupService.generateExportTableBySearchConditions(userPrincipal.getEmail(), name, mentorEmail, menteeEmail, groupCategory, timeStart1, timeEnd1, timeStart2, timeEnd2, status, remainColumns);
        return response;
    }

    @Operation(summary = "Get group workspace (Mobile)",
            description = "Get central workspace of group contains channels and private messages",
            tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = GroupDetailResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @GetMapping("/{groupId}/workspace")
    public ResponseEntity<GroupDetailResponse> getWorkspace(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                                 @PathVariable String groupId) {
        GroupDetailResponse workspace = groupService.getGroupWorkspace(userPrincipal, groupId);
        if (workspace == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(workspace);
    }

    @Operation(summary = "Mark mentee in group (Mobile)",
            description = "Can mark and push them on the list mentees of group",
            tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Do successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Void.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @PostMapping("/{groupId}/star")
    public ResponseEntity<Void> markMentee(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                           @PathVariable String groupId,
                                           @RequestParam String menteeId) {
        boolean isMarked = groupService.markMentee(userPrincipal, groupId, menteeId);
        if (!isMarked) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Unark mentee in group (Mobile)",
            description = "Can unmark mentees of group",
            tags = "Group APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Do successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Void.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @DeleteMapping("/{groupId}/star")
    public ResponseEntity<Void> unmarkMentee(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                           @PathVariable String groupId,
                                           @RequestParam String menteeId) {
        boolean isMarked = groupService.unmarkMentee(userPrincipal, groupId, menteeId);
        if (!isMarked) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }
}
