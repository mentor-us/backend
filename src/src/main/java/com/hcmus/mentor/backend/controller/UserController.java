package com.hcmus.mentor.backend.controller;

import an.awesome.pipelinr.Pipeline;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.payload.ApiResponseDto;
import com.hcmus.mentor.backend.controller.payload.request.*;
import com.hcmus.mentor.backend.controller.payload.response.users.ProfileResponse;
import com.hcmus.mentor.backend.controller.payload.response.users.UserDetailResponse;
import com.hcmus.mentor.backend.controller.usecase.user.addaddtionalemail.AddAdditionalEmailCommand;
import com.hcmus.mentor.backend.controller.usecase.user.removeadditionalemail.RemoveAdditionalEmailCommand;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.UserRole;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.CurrentUser;
import com.hcmus.mentor.backend.security.principal.userdetails.CustomerUserDetails;
import com.hcmus.mentor.backend.service.UserService;
import com.hcmus.mentor.backend.service.dto.UserServiceDto;
import io.minio.errors.*;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.hcmus.mentor.backend.controller.payload.returnCode.UserReturnCode.NOT_FOUND;

/**
 * User controller.
 */
@Tag(name = "users")
@RestController
@RequestMapping("api/users")
@SecurityRequirement(name = "bearer")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final Pipeline pipeline;

    /**
     * Retrieve all users.
     *
     * @return ApiResponseDto<List < User>> - Response containing a list of users.
     */
    @GetMapping(value = "all")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<List<User>> all() {
        UserServiceDto userReturn = userService.listAll();
        return new ApiResponseDto(userReturn.getData(), userReturn.getReturnCode(), userReturn.getMessage());
    }

    /**
     * Retrieve all users with pagination.
     *
     * @param customerUserDetails Current authenticated user's principal.
     * @param email               Email filter.
     * @param page                Page number.
     * @param size                Number of items per page.
     * @return ApiResponseDto<Page < User>> - Response containing a paginated list of users.
     */
    @GetMapping(value = "all-paging")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Page<User>> allPaging(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam(defaultValue = "") String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        Pageable pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        String emailUser = customerUserDetails.getEmail();
        UserServiceDto userReturn =
                userService.listAllPaging(emailUser, pageRequest);
        if (userReturn.getData() != null) {
            return new ApiResponseDto(
                    pagingResponse((Page<User>) userReturn.getData()),
                    userReturn.getReturnCode(),
                    userReturn.getMessage());
        }
        return new ApiResponseDto(userReturn.getData(), userReturn.getReturnCode(), userReturn.getMessage());
    }


    /**
     * Retrieve users by email with pagination.
     *
     * @param customerUserDetails Current authenticated user's principal.
     * @param email               Email filter.
     * @param page                Page number.
     * @param size                Number of items per page.
     * @return ApiResponseDto<Page < User>> - Response containing a paginated list of users.
     */
    @GetMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Page<User>> listByEmail(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam(defaultValue = "") String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        Pageable pageRequest = PageRequest.of(page, size);
        String emailUser = customerUserDetails.getEmail();
        UserServiceDto userReturn =
                userService.listByEmail(emailUser, email, pageRequest);
        return new ApiResponseDto(
                pagingResponse((Page<User>) userReturn.getData()),
                userReturn.getReturnCode(),
                userReturn.getMessage());
    }

    /**
     * Retrieve all users by email without pagination.
     *
     * @param customerUserDetails Current authenticated user's principal.
     * @param email               Email filter.
     * @return ApiResponseDto<List < User>> - Response containing a list of users.
     */
    @GetMapping("allByEmail")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<List<User>> listAllByEmail(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam(defaultValue = "") String email) {
        String emailUser = customerUserDetails.getEmail();
        UserServiceDto userReturn = userService.listAllByEmail(emailUser, email);
        return new ApiResponseDto(userReturn.getData(), userReturn.getReturnCode(), userReturn.getMessage());
    }

    /**
     * Retrieve user by ID.
     *
     * @param id User ID.
     * @return ApiResponseDto<ProfileResponse> - Response containing user profile information.
     */
    @GetMapping("{id}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<ProfileResponse> get(@PathVariable String id) {
        Optional<User> userWrapper = userRepository.findById(id);
        if (!userWrapper.isPresent()) {
            return ApiResponseDto.notFound(NOT_FOUND);
        }
        ProfileResponse response = ProfileResponse.from(userWrapper.get());
        return ApiResponseDto.success(response);
    }

    /**
     * Retrieve own profile.
     *
     * @param loggedUser Current authenticated user's principal.
     * @return ApiResponseDto<ProfileResponse> - Response containing own user profile information.
     */
    @SneakyThrows
    @GetMapping("me")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<ProfileResponse> getCurrentUser(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails loggedUser) {
        var user = userService.findById(loggedUser.getId());
        if (user.isEmpty()) {
            throw new DomainException(String.format("User with id %s not found", loggedUser.getId()));
        }
        var profileResponse = ProfileResponse.from(user.get());
        return ApiResponseDto.success(profileResponse);
    }

    /**
     * Activate user account.
     *
     * @param id User ID to activate.
     * @return ApiResponseDto<User> - Response containing the activated user.
     */
    @PostMapping("{id}/activate")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<User> activate(@PathVariable String id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            return new ApiResponseDto(false, "No Account", NOT_FOUND);
        }
        User user = userOptional.get();
        user.activate();
        userRepository.save(user);

        return ApiResponseDto.success(user);
    }

    /**
     * Update an existing user.
     *
     * @param customerUserDetails Current authenticated user's principal.
     * @param id                  User ID to update.
     * @param request             UpdateUserForAdminRequest containing updated information.
     * @return ApiResponseDto<User> - Response containing the updated user.
     */
    @ApiResponse(responseCode = "200")
    @PatchMapping("{id}/admin")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<User> update(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable String id,
            @RequestBody UpdateUserForAdminRequest request) {
        String emailUser = customerUserDetails.getEmail();
        UserServiceDto userReturn = userService.updateUserForAdmin(emailUser, id, request);
        return new ApiResponseDto(
                userReturn.getData(), userReturn.getReturnCode(), userReturn.getMessage());
    }

    /**
     * Delete an existing user.
     *
     * @param customerUserDetails Current authenticated user's principal.
     * @param id                  User ID to delete.
     * @return ApiResponseDto - Response containing the result of the delete operation.
     */
    @DeleteMapping("{id}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto delete(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails, @PathVariable String id) {
        String emailUser = customerUserDetails.getEmail();
        UserServiceDto userReturn = userService.deleteUser(emailUser, id);
        return new ApiResponseDto(
                userReturn.getData(), userReturn.getReturnCode(), userReturn.getMessage());
    }

    /**
     * Add a new user.
     *
     * @param customerUserDetails Current authenticated user's principal.
     * @param request             AddUserRequest containing user information.
     * @return ApiResponseDto - Response containing the added user.
     */
    @PostMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<User> add(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestBody AddUserRequest request) {
        String emailUser = customerUserDetails.getEmail();
        UserServiceDto userReturn = userService.addUser(emailUser, request);
        return new ApiResponseDto(
                userReturn.getData(), userReturn.getReturnCode(), userReturn.getMessage());
    }

    /**
     * Update user profile.
     *
     * @param customerUserDetails Current authenticated user's principal.
     * @param userId              User ID to update.
     * @param request             UpdateUserRequest containing updated information.
     * @return ResponseEntity<UserServiceImpl.UserReturnService> - Response entity containing the updated user information.
     */
    @PatchMapping("{userId}/profile")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<UserServiceDto> updateProfile(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable String userId,
            @RequestBody UpdateUserRequest request) {
        UserServiceDto userReturn = userService.updateUser(userId, request);
        return ResponseEntity.ok(userReturn);
    }

    /**
     * Find users with multiple filters.
     *
     * @param customerUserDetails Current authenticated user's principal.
     * @param name                User name filter.
     * @param emailSearch         Email search filter.
     * @param status              User status filter.
     * @param role                User role filter.
     * @param page                Page number.
     * @param size                Number of items per page.
     * @return ApiResponseDto<Page < Group>> - Response containing a paginated list of users.
     * @throws InvocationTargetException If there is an issue with invocation target.
     * @throws NoSuchMethodException     If the specified method is not found.
     * @throws IllegalAccessException    If there is an illegal access to the method.
     */
    @GetMapping("find")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Page<Group>> get(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String emailSearch,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) UserRole role,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "25") Integer size)
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        String email = customerUserDetails.getEmail();
        FindUserRequest request = new FindUserRequest(name, emailSearch, status, role);
        UserServiceDto userReturn =
                userService.findUsers(email, request, page, size);
        return new ApiResponseDto(
                pagingResponse((Page<User>) userReturn.getData()),
                userReturn.getReturnCode(),
                userReturn.getMessage());
    }

    /**
     * Delete multiple users.
     *
     * @param customerUserDetails Current authenticated user's principal.
     * @param ids                 List of User IDs to delete.
     * @return ApiResponseDto - Response containing the result of the delete operation.
     */
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    @DeleteMapping("")
    public ApiResponseDto deleteMultiple(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestBody List<String> ids) {
        String emailUser = customerUserDetails.getEmail();
        UserServiceDto userReturn = userService.deleteMultiple(emailUser, ids);
        return new ApiResponseDto(
                userReturn.getData(), userReturn.getReturnCode(), userReturn.getMessage());
    }

    /**
     * Disable multiple users.
     *
     * @param customerUserDetails Current authenticated user's principal.
     * @param ids                 List of User IDs to disable.
     * @return ApiResponseDto - Response containing the result of the disable operation.
     */
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    @PatchMapping("disable")
    public ApiResponseDto disableMultiple(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestBody List<String> ids) {
        String emailUser = customerUserDetails.getEmail();
        UserServiceDto userReturn = userService.disableMultiple(emailUser, ids);
        return new ApiResponseDto(
                userReturn.getData(), userReturn.getReturnCode(), userReturn.getMessage());
    }

    /**
     * Enable multiple users.
     *
     * @param customerUserDetails Current authenticated user's principal.
     * @param ids                 List of User IDs to enable.
     * @return ApiResponseDto - Response containing the result of the enable operation.
     */
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    @PatchMapping("enable")
    public ApiResponseDto enableMultiple(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestBody List<String> ids) {
        String emailUser = customerUserDetails.getEmail();
        UserServiceDto userReturn = userService.enableMultiple(emailUser, ids);
        return new ApiResponseDto(
                userReturn.getData(), userReturn.getReturnCode(), userReturn.getMessage());
    }

    /**
     * Get user detail by ID (for admin).
     *
     * @param customerUserDetails Current authenticated user's principal.
     * @param id                  User ID to get details.
     * @return ApiResponseDto<UserDetailResponse> - Response containing user details.
     */
    @GetMapping("{id}/detail")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<UserDetailResponse> getDetail(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails, @PathVariable String id) {
        String emailUser = customerUserDetails.getEmail();
        UserServiceDto userReturn = userService.getDetail(emailUser, id);
        return new ApiResponseDto(
                userReturn.getData(), userReturn.getReturnCode(), userReturn.getMessage());
    }

    /**
     * Update avatar.
     *
     * @param customerUserDetails Current authenticated user's principal.
     * @param file                MultipartFile containing the avatar image.
     * @return ApiResponseDto<String> - Response containing the URL of the updated avatar.
     * @throws GeneralSecurityException  If there is a general security exception.
     * @throws IOException               If there is an I/O exception.
     * @throws ServerException           If there is a server exception.
     * @throws InsufficientDataException If there is insufficient data.
     * @throws ErrorResponseException    If there is an error response.
     * @throws InvalidResponseException  If the response is invalid.
     * @throws XmlParserException        If there is an XML parsing exception.
     * @throws InternalException         If there is an internal exception.
     */
    @PostMapping("avatar")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<String> updateAvatar(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam MultipartFile file)
            throws GeneralSecurityException, IOException, ServerException, InsufficientDataException, ErrorResponseException, InvalidResponseException, XmlParserException, InternalException {
        UserServiceDto userReturn =
                userService.updateAvatar(customerUserDetails.getId(), file);
        return new ApiResponseDto(
                userReturn.getData(), userReturn.getReturnCode(), userReturn.getMessage());
    }

    /**
     * Update wallpaper.
     *
     * @param customerUserDetails Current authenticated user's principal.
     * @param file                MultipartFile containing the wallpaper image.
     * @return ApiResponseDto<String> - Response containing the URL of the updated wallpaper.
     * @throws GeneralSecurityException  If there is a general security exception.
     * @throws IOException               If there is an I/O exception.
     * @throws ServerException           If there is a server exception.
     * @throws InsufficientDataException If there is insufficient data.
     * @throws ErrorResponseException    If there is an error response.
     * @throws InvalidResponseException  If the response is invalid.
     * @throws XmlParserException        If there is an XML parsing exception.
     * @throws InternalException         If there is an internal exception.
     */
    @PostMapping("wallpaper")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<String> updateWallpaper(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam MultipartFile file)
            throws GeneralSecurityException, IOException, ServerException, InsufficientDataException, ErrorResponseException, InvalidResponseException, XmlParserException, InternalException {
        UserServiceDto userReturn =
                userService.updateWallpaper(customerUserDetails.getId(), file);
        return new ApiResponseDto(
                userReturn.getData(), userReturn.getReturnCode(), userReturn.getMessage());
    }

    /**
     * Export users table.
     *
     * @param customerUserDetails Current authenticated user's principal.
     * @param remainColumns       List of columns to remain in the exported table.
     * @return ResponseEntity<Resource> - Response entity containing the exported table as a resource.
     * @throws IOException If there is an I/O exception during the export process.
     */
    @GetMapping("export")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Resource> export(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam(defaultValue = "") List<String> remainColumns)
            throws IOException {
        return userService.generateExportTable(customerUserDetails.getEmail(), remainColumns);
    }

    /**
     * Import multiple users.
     *
     * @param customerUserDetails Current authenticated user's principal.
     * @param file                MultipartFile containing the template file for user import.
     * @return ApiResponseDto<List < Group>> - Response containing the imported users.
     * @throws IOException If there is an I/O exception during the import process.
     */
    @PostMapping(value = "import", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<List<Group>> importUsers(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam("file") MultipartFile file)
            throws IOException {
        String email = customerUserDetails.getEmail();
        UserServiceDto userReturn = userService.importUsers(email, file);
        return new ApiResponseDto(
                userReturn.getData(), userReturn.getReturnCode(), userReturn.getMessage());
    }

    /**
     * Export mentor groups user table.
     *
     * @param customerUserDetails Current authenticated user's principal.
     * @param userId              User ID for whom to export mentor groups.
     * @param remainColumns       List of columns to remain in the exported table.
     * @return ResponseEntity<Resource> - Response entity containing the exported table as a resource.
     * @throws IOException If there is an I/O exception during the export process.
     */
    @GetMapping("{userId}/mentorGroups/export")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Resource> exportMentorGroups(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable String userId,
            @RequestParam(defaultValue = "") List<String> remainColumns)
            throws IOException {
        return userService.generateExportTableMembers(
                customerUserDetails.getEmail(), remainColumns, userId, "MENTOR");
    }

    /**
     * Export mentee groups user table.
     *
     * @param customerUserDetails Current authenticated user's principal.
     * @param userId              User ID for whom to export mentee groups.
     * @param remainColumns       List of columns to remain in the exported table.
     * @return ResponseEntity<Resource> - Response entity containing the exported table as a resource.
     * @throws IOException If there is an I/O exception during the export process.
     */
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    @GetMapping("{userId}/menteeGroups/export")
    public ResponseEntity<Resource> exportMenteeGroups(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable String userId,
            @RequestParam(defaultValue = "") List<String> remainColumns)
            throws IOException {
        return userService.generateExportTableMembers(customerUserDetails.getEmail(), remainColumns, userId, "MENTEE");
    }

    /**
     * Export users table by search conditions.
     *
     * @param customerUserDetails Current authenticated user's principal.
     * @param name                User name filter.
     * @param emailSearch         Email search filter.
     * @param status              User status filter.
     * @param role                User role filter.
     * @param remainColumns       List of columns to remain in the exported table.
     * @return ResponseEntity<Resource> - Response entity containing the exported table as a resource.
     * @throws IOException If there is an I/O exception during the export process.
     */
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    @GetMapping("export/search")
    public ResponseEntity<Resource> exportBySearchConditions(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String emailSearch,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) UserRole role,
            @RequestParam(defaultValue = "") List<String> remainColumns)
            throws IOException {
        FindUserRequest request = new FindUserRequest(name, emailSearch, status, role);
        return userService.generateExportTableBySearchConditions(
                customerUserDetails.getEmail(), request, remainColumns);
    }

    /**
     * Add additional email to user.
     *
     * @param customerUserDetails Current authenticated user's principal.
     * @param userId              User ID to add an additional email.
     * @param request             AddAdditionEmailRequest containing the additional email.
     * @return ApiResponseDto<User> - Response containing the user with additional email added.
     */
    @PostMapping("{userId}/email/add")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<User> addAdditionalEmail(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable String userId,
            @RequestBody AddAdditionEmailRequest request) {
        var command = new AddAdditionalEmailCommand(userId, request.getAdditionalEmail());
        var userReturn = command.execute(pipeline);
        return new ApiResponseDto(userReturn.getData(), userReturn.getReturnCode(), userReturn.getMessage());
    }

    /**
     * Delete additional email of user.
     *
     * @param customerUserDetails Current authenticated user's principal.
     * @param userId              User ID to delete an additional email.
     * @param request             RemoveAdditionalEmailRequest containing the additional email to remove.
     * @return ApiResponseDto<User> - Response containing the user with additional email removed.
     */
    @DeleteMapping("{userId}/email/remove")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<User> deleteAdditionalEmail(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable String userId,
            @RequestBody RemoveAdditionalEmailRequest request) {
        var command = new RemoveAdditionalEmailCommand(userId, request.getAdditionalEmail());
        var userReturn = command.execute(pipeline);
        return new ApiResponseDto(userReturn.getData(), userReturn.getReturnCode(), userReturn.getMessage());
    }

    private Map<String, Object> pagingResponse(Page<User> users) {
        Map<String, Object> response = new HashMap<>();
        response.put("content", users.getContent());
        response.put("currentPage", users.getNumber());
        response.put("totalItems", users.getTotalElements());
        response.put("totalPages", users.getTotalPages());
        return response;
    }
}
