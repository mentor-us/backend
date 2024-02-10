package com.hcmus.mentor.backend.controller;

import com.hcmus.mentor.backend.controller.payload.ApiResponseDto;
import com.hcmus.mentor.backend.controller.payload.request.CreateRoleRequest;
import com.hcmus.mentor.backend.controller.payload.request.UpdateRoleRequest;
import com.hcmus.mentor.backend.domain.Role;
import com.hcmus.mentor.backend.security.CurrentUser;
import com.hcmus.mentor.backend.security.UserPrincipal;
import com.hcmus.mentor.backend.service.RoleService;
import com.hcmus.mentor.backend.service.dto.RoleServiceDto;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Role controller.
 */
@Tag(name = "role")
@RestController
@RequestMapping("api/roles")
@SecurityRequirement(name = "bearer")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * Get all roles in the system.
     *
     * @param userPrincipal The current user's principal information.
     * @return APIResponse containing the list of roles in the system.
     */
    @GetMapping("all")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<List<Role>> all(@CurrentUser UserPrincipal userPrincipal) {
        String emailUser = userPrincipal.getEmail();
        RoleServiceDto roleServiceReturn = roleService.findAll(emailUser);
        return new ApiResponseDto(
                roleServiceReturn.getData(),
                roleServiceReturn.getReturnCode(),
                roleServiceReturn.getMessage());
    }

    /**
     * Get a specific role by ID.
     *
     * @param userPrincipal The current user's principal information.
     * @param id            The ID of the role to retrieve.
     * @return APIResponse containing the retrieved role or a not-found response.
     */
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    @GetMapping("{id}")
    public ApiResponseDto<Role> get(@CurrentUser UserPrincipal userPrincipal, @PathVariable String id) {
        String emailUser = userPrincipal.getEmail();
        RoleServiceDto roleServiceReturn = roleService.findById(emailUser, id);
        return new ApiResponseDto(
                roleServiceReturn.getData(),
                roleServiceReturn.getReturnCode(),
                roleServiceReturn.getMessage());
    }

    /**
     * Create a new role.
     *
     * @param userPrincipal The current user's principal information.
     * @param request       The request payload for creating a new role.
     * @return APIResponse containing the created role or a response indicating failure.
     */
    @PostMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Role> create(
            @CurrentUser UserPrincipal userPrincipal, @RequestBody CreateRoleRequest request) {
        String emailUser = userPrincipal.getEmail();
        RoleServiceDto roleServiceReturn = roleService.create(emailUser, request);
        return new ApiResponseDto(
                roleServiceReturn.getData(),
                roleServiceReturn.getReturnCode(),
                roleServiceReturn.getMessage());
    }

    /**
     * Update an existing role.
     *
     * @param userPrincipal The current user's principal information.
     * @param id            The ID of the role to update.
     * @param request       The request payload for updating the role.
     * @return APIResponse containing the updated role or a not-found response.
     */
    @PatchMapping("{id}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Role> update(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable String id,
            @RequestBody UpdateRoleRequest request) {
        String emailUser = userPrincipal.getEmail();
        RoleServiceDto roleServiceReturn = roleService.update(emailUser, id, request);
        return new ApiResponseDto(
                roleServiceReturn.getData(),
                roleServiceReturn.getReturnCode(),
                roleServiceReturn.getMessage());
    }

    /**
     * Delete roles.
     *
     * @param userPrincipal The current user's principal information.
     * @param ids           The list of role IDs to delete.
     * @return APIResponse containing the result of the delete operation.
     */
    @DeleteMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Role> delete(
            @CurrentUser UserPrincipal userPrincipal, @RequestBody List<String> ids) {
        String emailUser = userPrincipal.getEmail();
        RoleServiceDto roleServiceReturn = roleService.deleteMultiple(emailUser, ids);
        return new ApiResponseDto(
                roleServiceReturn.getData(),
                roleServiceReturn.getReturnCode(),
                roleServiceReturn.getMessage());
    }
}
