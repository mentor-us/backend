package com.hcmus.mentor.backend.controller;

import com.hcmus.mentor.backend.controller.payload.ApiResponseDto;
import com.hcmus.mentor.backend.controller.payload.request.*;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.GroupCategory;
import com.hcmus.mentor.backend.domain.constant.GroupCategoryPermission;
import com.hcmus.mentor.backend.security.CurrentUser;
import com.hcmus.mentor.backend.security.UserPrincipal;
import com.hcmus.mentor.backend.service.dto.GroupCategoryServiceDto;
import com.hcmus.mentor.backend.service.GroupCategoryService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Group category controller.
 */
@Tag(name = "group categories")
@RestController
@RequestMapping("api/group-categories")
@SecurityRequirement(name = "bearer")
@RequiredArgsConstructor
public class GroupCategoryController {

    private final GroupCategoryService groupCategoryService;

    /**
     * Retrieves all group categories.
     *
     * @return ResponseEntity containing a list of all group categories.
     */
    @GetMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<List<GroupCategory>> findAll() {
        return ApiResponseDto.success(groupCategoryService.findAll());
    }

    /**
     * Retrieves a group category by its ID.
     *
     * @param id The ID of the group category to retrieve.
     * @return ResponseEntity containing the group category information.
     */
    @GetMapping("{id}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<GroupCategory> findById(@PathVariable String id) {
        GroupCategoryServiceDto groupCategoryReturn = groupCategoryService.findById(id);
        return new ApiResponseDto(
                groupCategoryReturn.getData(),
                groupCategoryReturn.getReturnCode(),
                groupCategoryReturn.getMessage());
    }

    /**
     * Creates a new group category.
     *
     * @param userPrincipal The current user's principal information.
     * @param request       The request containing information to create a new group category.
     * @return ResponseEntity containing the newly created group category information.
     */
    @PostMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<GroupCategory> create(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @RequestBody CreateGroupCategoryRequest request) {
        String email = userPrincipal.getEmail();
        GroupCategoryServiceDto groupCategoryReturn = groupCategoryService.create(email, request);
        return new ApiResponseDto(
                groupCategoryReturn.getData(),
                groupCategoryReturn.getReturnCode(),
                groupCategoryReturn.getMessage());
    }

    /**
     * Updates an existing group category.
     *
     * @param userPrincipal The current user's principal information.
     * @param id            The ID of the group category to be updated.
     * @param request       The request containing updated information for the group category.
     * @return ResponseEntity containing the updated group category information.
     */
    @PatchMapping("{id}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<GroupCategory> update(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable String id,
            @RequestBody UpdateGroupCategoryRequest request) {
        String email = userPrincipal.getEmail();
        GroupCategoryServiceDto groupCategoryReturn = groupCategoryService.update(email, id, request);
        return new ApiResponseDto(
                groupCategoryReturn.getData(),
                groupCategoryReturn.getReturnCode(),
                groupCategoryReturn.getMessage());
    }

    /**
     * Deletes an existing group category.
     *
     * @param userPrincipal The current user's principal information.
     * @param request       The request containing information to delete the group category.
     * @param id            The ID of the group category to be deleted.
     * @return APIResponse indicating the success of the deletion operation.
     */
    @DeleteMapping("{id}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto delete(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @RequestBody DeleteGroupCategoryRequest request,
            @PathVariable String id) {
        String email = userPrincipal.getEmail();
        String newGroupCategoryId = request.getNewGroupCategoryId();
        GroupCategoryServiceDto groupCategoryReturn =
                groupCategoryService.delete(email, id, newGroupCategoryId);
        return new ApiResponseDto(
                groupCategoryReturn.getData(),
                groupCategoryReturn.getReturnCode(),
                groupCategoryReturn.getMessage());
    }

    /**
     * Finds group categories with multiple filters.
     *
     * @param userPrincipal The current user's principal information.
     * @param name          The name filter for group categories.
     * @param description   The description filter for group categories.
     * @param status        The status filter for group categories.
     * @param page          The page number for pagination.
     * @param size          The page size for pagination.
     * @return APIResponse containing the paginated list of group categories.
     */
    @GetMapping("find")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Page<Group>> get(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "") String description,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "25") Integer size) {
        String email = userPrincipal.getEmail();
        FindGroupCategoryRequest request = new FindGroupCategoryRequest(name, description, status);
        GroupCategoryServiceDto groupCategoryReturn =
                groupCategoryService.findGroupCategories(email, request, page, size);
        return new ApiResponseDto(
                pagingResponse((Page<GroupCategory>) groupCategoryReturn.getData()),
                groupCategoryReturn.getReturnCode(),
                groupCategoryReturn.getMessage());
    }

    /**
     * Deletes multiple existing group categories.
     *
     * @param userPrincipal The current user's principal information.
     * @param request       The request containing information to delete multiple group categories.
     * @return APIResponse indicating the success of the deletion operation.
     */
    @DeleteMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto deleteMultiple(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @RequestBody DeleteMultipleGroupCategoryRequest request) {
        String email = userPrincipal.getEmail();
        List<String> ids = request.getIds();
        String newGroupCategoryId = request.getNewGroupCategoryId();
        GroupCategoryServiceDto groupCategoryReturn =
                groupCategoryService.deleteMultiple(email, ids, newGroupCategoryId);
        return new ApiResponseDto(
                groupCategoryReturn.getData(),
                groupCategoryReturn.getReturnCode(),
                groupCategoryReturn.getMessage());
    }

    /**
     * Exports group categories table based on search conditions.
     *
     * @param userPrincipal The current user's principal information.
     * @param name          The name filter for group categories.
     * @param description   The description filter for group categories.
     * @param status        The status filter for group categories.
     * @param remainColumns The columns to include in the export.
     * @return ResponseEntity containing the exported group categories table as a Resource.
     * @throws IOException If an I/O error occurs during the export process.
     */
    @GetMapping(value = "export/search")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Resource> exportBySearchConditions(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "") String description,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "") List<String> remainColumns)
            throws IOException {
        FindGroupCategoryRequest request = new FindGroupCategoryRequest(name, description, status);
        return groupCategoryService.generateExportTableBySearchConditions(userPrincipal.getEmail(), request, remainColumns);
    }

    /**
     * Exports the entire group categories table.
     *
     * @param userPrincipal The current user's principal information.
     * @param remainColumns The columns to include in the export.
     * @return ResponseEntity containing the exported group categories table as a Resource.
     * @throws IOException If an I/O error occurs during the export process.
     */
    @GetMapping(value = "export")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Resource> export(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "") List<String> remainColumns)
            throws IOException {
        return groupCategoryService.generateExportTable(userPrincipal.getEmail(), remainColumns);
    }

    /**
     * Retrieves all permission systems for group categories.
     *
     * @return ResponseEntity containing an array of EnumWithDescription representing all permission systems.
     */
    @GetMapping("permissions")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<?> getAllPermissions() {
        GroupCategoryPermission[] permissions = GroupCategoryPermission.values();
        EnumWithDescription[] enumsWithDescription = new EnumWithDescription[permissions.length];

        for (int i = 0; i < permissions.length; i++) {
            GroupCategoryPermission permission = permissions[i];
            enumsWithDescription[i] = new EnumWithDescription(permission.name(), permission.getDescription());
        }

        return ResponseEntity.ok(enumsWithDescription);
    }

    private Map<String, Object> pagingResponse(Page<GroupCategory> groupCategories) {
        Map<String, Object> response = new HashMap<>();
        response.put("content", groupCategories.getContent());
        response.put("currentPage", groupCategories.getNumber());
        response.put("totalItems", groupCategories.getTotalElements());
        response.put("totalPages", groupCategories.getTotalPages());
        return response;
    }

    @Getter
    private static class EnumWithDescription {
        private final String name;
        private final String description;

        public EnumWithDescription(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }
}
