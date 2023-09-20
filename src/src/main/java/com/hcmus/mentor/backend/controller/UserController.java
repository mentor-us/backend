package com.hcmus.mentor.backend.controller;

import com.hcmus.mentor.backend.entity.Group;
import com.hcmus.mentor.backend.entity.User;
import com.hcmus.mentor.backend.exception.ResourceNotFoundException;
import com.hcmus.mentor.backend.payload.APIResponse;
import com.hcmus.mentor.backend.payload.request.*;
import com.hcmus.mentor.backend.payload.response.users.ProfileResponse;
import com.hcmus.mentor.backend.payload.response.users.UserDetailResponse;
import com.hcmus.mentor.backend.payload.returnCode.UserReturnCode;
import com.hcmus.mentor.backend.repository.ProfileRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.CurrentUser;
import com.hcmus.mentor.backend.security.UserPrincipal;
import com.hcmus.mentor.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.hcmus.mentor.backend.payload.returnCode.InvalidPermissionCode.INVALID_PERMISSION_STRING;
import static com.hcmus.mentor.backend.payload.returnCode.UserReturnCode.NOT_FOUND;

@Tag(name = "User APIs", description = "REST APIs for User collections")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    private final ProfileRepository profileRepository;

    private final UserService userService;

    public UserController(UserRepository userRepository, ProfileRepository profileRepository, UserService userService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.userService = userService;
    }

    @Operation(summary = "Retrieve all users",
            description = "Retrieve all user information on system",
            tags = "User APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retrieve successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            )})
    @GetMapping(value = "/all")
    public APIResponse<List<User>> all() {
        UserService.UserReturnService userReturn = userService.listAll();
        return new APIResponse(userReturn.getData(), userReturn.getReturnCode(),
                userReturn.getMessage());    }

    @Operation(summary = "Retrieve all users",
            description = "Retrieve all user information on system",
            tags = "User APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retrieve successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            )})
    @GetMapping(value = "/all-paging")
    public APIResponse<Page<User>> allPaging(@ApiIgnore @CurrentUser UserPrincipal userPrincipal,
                                             @RequestParam(defaultValue = "") String email,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "25") int size) {
        Pageable pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        String emailUser = userPrincipal.getEmail();
        UserService.UserReturnService userReturn = userService.listAllPaging(emailUser, pageRequest);
        if(userReturn.getData() != null){
            return new APIResponse(pagingResponse((Page<User>) userReturn.getData()), userReturn.getReturnCode(),
                    userReturn.getMessage());
        }
        return new APIResponse(userReturn.getData(), userReturn.getReturnCode(),
                userReturn.getMessage());
    }

    private Map<String, Object> pagingResponse(Page<User> users) {
        Map<String, Object> response = new HashMap<>();
        response.put("content", users.getContent());
        response.put("currentPage", users.getNumber());
        response.put("totalItems", users.getTotalElements());
        response.put("totalPages", users.getTotalPages());
        return response;
    }

    @Operation(summary = "Retrieve users by email (Paging)",
            description = "Retrieve all users information matched with needed email, response will be paged",
            tags = "User APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retrieve successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            )})
    @GetMapping(value = {"/", ""})
    public APIResponse<Page<User>> listByEmail(@ApiIgnore @CurrentUser UserPrincipal userPrincipal,
                                   @RequestParam(defaultValue = "") String email,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "25") int size) {
        Pageable pageRequest = PageRequest.of(page, size);
        String emailUser = userPrincipal.getEmail();
        UserService.UserReturnService userReturn = userService.listByEmail(emailUser, email, pageRequest);
        return new APIResponse(pagingResponse((Page<User>) userReturn.getData()), userReturn.getReturnCode(),
                userReturn.getMessage());
    }

    @Operation(summary = "Retrieve all users by email (No paging)",
            description = "Retrieve all users information matched with needed email",
            tags = "User APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retrieve successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            )})
    @GetMapping(value = {"/allByEmail", ""})
    public APIResponse<List<User>> listAllByEmail(@ApiIgnore @CurrentUser UserPrincipal userPrincipal,
                                   @RequestParam(defaultValue = "") String email) {
        String emailUser = userPrincipal.getEmail();
        UserService.UserReturnService userReturn = userService.listAllByEmail(emailUser, email);
        return new APIResponse(userReturn.getData(), userReturn.getReturnCode(), userReturn.getMessage());
    }

    @Operation(summary = "Retrieve user by id",
            description = "Retrieve user information by user id",
            tags = "User APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retrieve successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            )})
    @GetMapping("/{id}")
    public APIResponse<ProfileResponse> get(@PathVariable String id) {
        Optional<User> userWrapper = userRepository.findById(id);
        if (!userWrapper.isPresent()) {
            return APIResponse.notFound(NOT_FOUND);
        }
        ProfileResponse response = ProfileResponse.from(userWrapper.get());
        return APIResponse.success(response);
    }

    @Operation(summary = "Retrieve own profile",
            description = "Retrieve your own profile",
            tags = "User APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retrieve successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            )})
    @GetMapping("/me")
    public APIResponse<ProfileResponse> getCurrentUser(@ApiIgnore @CurrentUser UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
        ProfileResponse profileResponse = ProfileResponse.from(user);
        return APIResponse.success(profileResponse);
    }

    @Operation(summary = "Activate account",
            description = "Account will be deactived after creating, admin need activate account by user id",
            tags = "User APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Activate successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            )})
    @PostMapping(value = "/{id}/activate")
    public APIResponse<User> activate(@PathVariable String id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            return new APIResponse(false, "No Account", NOT_FOUND);
        }
        User user = userOptional.get();
        user.activate();
        userRepository.save(user);

        return APIResponse.success(user);
    }

    @Operation(summary = "Update an existing user",
            description = "Update an existing user information by user id",
            tags = "User APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Update successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = UserReturnCode.NOT_FOUND_STRING, description = "Not found user"),
    })
    @PatchMapping (value = "/{id}/admin")
    public APIResponse<User> update(@ApiIgnore @CurrentUser UserPrincipal userPrincipal,
                                    @PathVariable String id,
                                    @RequestBody UpdateUserForAdminRequest request) {
        String emailUser = userPrincipal.getEmail();
        UserService.UserReturnService userReturn = userService.updateUserForAdmin(emailUser, id, request);
        return new APIResponse(userReturn.getData(), userReturn.getReturnCode(), userReturn.getMessage());
    }

    @Operation(summary = "Delete an existing user",
            description = "Delete an existing user information on system",
            tags = "User APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Delete successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = UserReturnCode.NOT_FOUND_STRING, description = "Not found user"),
    })
    @DeleteMapping(value = "/{id}")
    public APIResponse delete(@ApiIgnore @CurrentUser UserPrincipal userPrincipal,
                              @PathVariable String id) {
        String emailUser = userPrincipal.getEmail();
        UserService.UserReturnService userReturn = userService.deleteUser(emailUser, id);
        return new APIResponse(userReturn.getData(), userReturn.getReturnCode(), userReturn.getMessage());
    }

    @Operation(summary = "Add new user", description = "Create new user on system", tags = "User APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Add successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            )})
    @PostMapping (value = {"/", ""})
    public APIResponse<User> add(@ApiIgnore @CurrentUser UserPrincipal userPrincipal,
                           @RequestBody AddUserRequest request) {
        String emailUser = userPrincipal.getEmail();
        UserService.UserReturnService userReturn = userService.addUser(emailUser, request);
        return new APIResponse(userReturn.getData(), userReturn.getReturnCode(), userReturn.getMessage());
    }

    @Operation(summary = "Update user profile",
            description = "Update a existing user in system",
            tags = "User APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserService.UserReturnService.class)))),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @PatchMapping("/{userId}/profile")
    public ResponseEntity<UserService.UserReturnService> updateProfile(
            @ApiIgnore @CurrentUser UserPrincipal userPrincipal,
            @PathVariable String userId,
            @RequestBody UpdateUserRequest request) {
        UserService.UserReturnService userReturn = userService.updateUser(userId, request);
        return ResponseEntity.ok(userReturn);
    }
    @Operation(summary = "Find users", description = "Find users with multiple filters", tags = "User APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retrieve successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            )
    })
    @GetMapping("/find")
    public APIResponse<Page<Group>> get(@ApiIgnore @CurrentUser UserPrincipal userPrincipal,
                                        @RequestParam(required = false) String name,
                                        @RequestParam(required = false) String emailSearch,
                                        @RequestParam(required = false) Boolean status,
                                        @RequestParam(required = false) User.Role role,
                                        @RequestParam(defaultValue = "0") Integer page,
                                        @RequestParam(defaultValue = "25") Integer size) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        String email = userPrincipal.getEmail();
        FindUserRequest request = new FindUserRequest(name, emailSearch, status, role);
        UserService.UserReturnService userReturn = userService.findUsers(email, request, page, size);
        return new APIResponse(pagingResponse((Page<User>) userReturn.getData()), userReturn.getReturnCode(), userReturn.getMessage());
    }
    @Operation(summary = "Delete multiple users",
            description = "Delete multiple users on system",
            tags = "User APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Delete successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            )})
    @DeleteMapping(value = {"/", ""})
    public APIResponse deleteMultiple(@ApiIgnore @CurrentUser UserPrincipal userPrincipal,
                              @RequestBody List<String> ids) {
        String emailUser = userPrincipal.getEmail();
        UserService.UserReturnService userReturn = userService.deleteMultiple(emailUser, ids);
        return new APIResponse(userReturn.getData(), userReturn.getReturnCode(), userReturn.getMessage());
    }

    @Operation(summary = "Disable multiple users",
            description = "Disable multiple users on system",
            tags = "User APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disable successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = UserReturnCode.NOT_FOUND_STRING, description = "Not found user"),
    })
    @PatchMapping(value = "/disable")
    public APIResponse disableMultiple(@ApiIgnore @CurrentUser UserPrincipal userPrincipal,
                                      @RequestBody List<String> ids) {
        String emailUser = userPrincipal.getEmail();
        UserService.UserReturnService userReturn = userService.disableMultiple(emailUser, ids);
        return new APIResponse(userReturn.getData(), userReturn.getReturnCode(), userReturn.getMessage());
    }

    @Operation(summary = "Enable multiple users",
            description = "Enable multiple users on system",
            tags = "User APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Enable successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = UserReturnCode.NOT_FOUND_STRING, description = "Not found user"),
    })
    @PatchMapping(value = "/enable")
    public APIResponse enableMultiple(@ApiIgnore @CurrentUser UserPrincipal userPrincipal,
                                       @RequestBody List<String> ids) {
        String emailUser = userPrincipal.getEmail();
        UserService.UserReturnService userReturn = userService.enableMultiple(emailUser, ids);
        return new APIResponse(userReturn.getData(), userReturn.getReturnCode(), userReturn.getMessage());
    }

    @Operation(summary = "Get user detail by id(for admin)",
            description = "Get user detail by user id",
            tags = "User APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get user detail successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))
            ),
            @ApiResponse(responseCode = UserReturnCode.NOT_FOUND_STRING, description = "Not found user"),
    })
    @GetMapping("/{id}/detail")
    public APIResponse<UserDetailResponse> getDetail(@ApiIgnore @CurrentUser UserPrincipal userPrincipal,
                                        @PathVariable String id) {
        String emailUser = userPrincipal.getEmail();
        UserService.UserReturnService userReturn = userService.getDetail(emailUser, id);
        return new APIResponse(userReturn.getData(), userReturn.getReturnCode(), userReturn.getMessage());
    }

    @Operation(summary = "Update avatar",
            description = "Upload image to update avatar of user",
            tags = "User APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Update successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))
            ),
            @ApiResponse(responseCode = UserReturnCode.NOT_FOUND_STRING, description = "Not found user"),
})
    @PostMapping("/avatar")
    public APIResponse<String> updateAvatar(@ApiIgnore @CurrentUser UserPrincipal userPrincipal,
                                            @RequestParam MultipartFile file)
            throws GeneralSecurityException, IOException {
        UserService.UserReturnService userReturn = userService.updateAvatar(userPrincipal.getId(), file);
        return new APIResponse(userReturn.getData(), userReturn.getReturnCode(), userReturn.getMessage());
    }

    @Operation(summary = "Update wallpaper",
            description = "Upload image to update wallpaper of user",
            tags = "User APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Update successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))
            ),
            @ApiResponse(responseCode = UserReturnCode.NOT_FOUND_STRING, description = "Not found user"),
    })
    @PostMapping("/wallpaper")
    public APIResponse<String> updateWallpaper(@ApiIgnore @CurrentUser UserPrincipal userPrincipal,
                                            @RequestParam MultipartFile file)
            throws GeneralSecurityException, IOException {
        UserService.UserReturnService userReturn = userService.updateWallpaper(userPrincipal.getId(), file);
        return new APIResponse(userReturn.getData(), userReturn.getReturnCode(), userReturn.getMessage());
    }

    @Operation(summary = "Export users table",
            description = "Export users table", tags = "User APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Export successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @GetMapping(value = "/export")
    public ResponseEntity<Resource> export(@ApiIgnore @CurrentUser UserPrincipal userPrincipal,
                                           @RequestParam(defaultValue = "") List<String> remainColumns) throws IOException {
        ResponseEntity<Resource> response = userService.generateExportTable(userPrincipal.getEmail(), remainColumns);
        return response;
    }

    @Operation(summary = "Import multiple users", description = "Import multiple users by template file", tags = "User APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Import successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = UserReturnCode.INVALID_TEMPLATE_STRING, description = "Invalid Template"),
            @ApiResponse(responseCode = UserReturnCode.DUPLICATE_USER_STRING, description = "User has been duplicated"),            @ApiResponse(responseCode = UserReturnCode.DUPLICATE_USER_STRING, description = "User has been duplicated"),
            @ApiResponse(responseCode = INVALID_PERMISSION_STRING, description = "Invalid permission"),
    })
    @PostMapping(value = "/import", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public APIResponse<List<Group>> importUsers(@ApiIgnore @CurrentUser UserPrincipal userPrincipal,
                                                 @RequestParam("file") MultipartFile file) throws IOException {
        String email = userPrincipal.getEmail();
        UserService.UserReturnService userReturn = userService.importUsers(email, file);
        return new APIResponse(userReturn.getData(), userReturn.getReturnCode(), userReturn.getMessage());
    }

    @Operation(summary = "Export mentor groups user table",
            description = "Export mentor groups users table", tags = "User APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Export successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @GetMapping(value = "/{userId}/mentorGroups/export")
    public ResponseEntity<Resource> exportMentorGroups(@ApiIgnore @CurrentUser UserPrincipal userPrincipal,
                                           @PathVariable String userId,
                                           @RequestParam(defaultValue = "") List<String> remainColumns) throws IOException {
        ResponseEntity<Resource> response = userService.generateExportTableMembers(userPrincipal.getEmail(), remainColumns, userId, "MENTOR");
        return response;
    }

    @Operation(summary = "Export mentee groups user table",
            description = "Export mentee groups users table", tags = "User APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Export successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @GetMapping(value = "/{userId}/menteeGroups/export")
    public ResponseEntity<Resource> exportMenteeGroups(@ApiIgnore @CurrentUser UserPrincipal userPrincipal,
                                                       @PathVariable String userId,
                                                       @RequestParam(defaultValue = "") List<String> remainColumns) throws IOException {
        ResponseEntity<Resource> response = userService.generateExportTableMembers(userPrincipal.getEmail(), remainColumns, userId, "MENTEE");
        return response;
    }

    @Operation(summary = "Export users table by search conditions",
            description = "Export users table", tags = "User APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Export successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @GetMapping(value = "/export/search")
    public ResponseEntity<Resource> exportBySearchConditions(@ApiIgnore @CurrentUser UserPrincipal userPrincipal,
                                        @RequestParam(required = false) String name,
                                        @RequestParam(required = false) String emailSearch,
                                        @RequestParam(required = false) Boolean status,
                                        @RequestParam(required = false) User.Role role,
                                        @RequestParam(defaultValue = "") List<String> remainColumns) throws IOException {
        FindUserRequest request = new FindUserRequest(name, emailSearch, status, role);
        ResponseEntity<Resource> response = userService.generateExportTableBySearchConditions(userPrincipal.getEmail(), request, remainColumns);
        return response;
    }
}
