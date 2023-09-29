package com.hcmus.mentor.backend.controller;

import com.hcmus.mentor.backend.entity.Role;
import com.hcmus.mentor.backend.payload.APIResponse;
import com.hcmus.mentor.backend.payload.request.CreateRoleRequest;
import com.hcmus.mentor.backend.payload.request.UpdateRoleRequest;
import com.hcmus.mentor.backend.payload.returnCode.RoleReturnCode;
import com.hcmus.mentor.backend.security.CurrentUser;
import com.hcmus.mentor.backend.security.UserPrincipal;
import com.hcmus.mentor.backend.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Role APIs", description = "REST APIs for Role collections")
@RestController
@RequestMapping("/api/roles")
@SecurityRequirement(name = "bearer")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @Operation(summary = "Get all roles",
            description = "Get all roles in system",
            tags = "Role APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get all roles successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))
            ),
    })
    @GetMapping("/all")
    public APIResponse<List<Role>> all(@CurrentUser UserPrincipal userPrincipal) {
        String emailUser = userPrincipal.getEmail();
        RoleService.RoleServiceReturn roleServiceReturn = roleService.findAll(emailUser);
        return new APIResponse(roleServiceReturn.getData(), roleServiceReturn.getReturnCode(), roleServiceReturn.getMessage());
    }

    @Operation(summary = "Get a role",
            description = "Get a role in system",
            tags = "Role APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get role successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))
            ),
            @ApiResponse(responseCode = RoleReturnCode.NOT_FOUND_STRING, description = "Not found role"),
    })
    @GetMapping("/{id}")
    public APIResponse<Role> get(@CurrentUser UserPrincipal userPrincipal,
                                 @PathVariable String id) {
        String emailUser = userPrincipal.getEmail();
        RoleService.RoleServiceReturn roleServiceReturn = roleService.findById(emailUser, id);
        return new APIResponse(roleServiceReturn.getData(), roleServiceReturn.getReturnCode(), roleServiceReturn.getMessage());
    }

    @Operation(summary = "Create a role",
            description = "Create a new role",
            tags = "Role APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Create role successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))
            ),
            @ApiResponse(responseCode = RoleReturnCode.DUPLICATE_ROLE_STRING, description = "Duplicate role"),
            @ApiResponse(responseCode = RoleReturnCode.NOT_FOUND_PERMISSION_STRING, description = "Not found permissions"),

    })
    @PostMapping("/")
    public APIResponse<Role> create(@CurrentUser UserPrincipal userPrincipal,
                                 @RequestBody CreateRoleRequest request) {
        String emailUser = userPrincipal.getEmail();
        RoleService.RoleServiceReturn roleServiceReturn = roleService.create(emailUser, request);
        return new APIResponse(roleServiceReturn.getData(), roleServiceReturn.getReturnCode(), roleServiceReturn.getMessage());
    }

    @Operation(summary = "Update a role",
            description = "Update a role",
            tags = "Role APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Update role successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))
            ),
            @ApiResponse(responseCode = RoleReturnCode.NOT_FOUND_STRING, description = "Not found role"),
            @ApiResponse(responseCode = RoleReturnCode.DUPLICATE_ROLE_STRING, description = "Duplicate role"),
            @ApiResponse(responseCode = RoleReturnCode.NOT_FOUND_PERMISSION_STRING, description = "Not found permissions"),
    })
    @PatchMapping("/{id}")
    public APIResponse<Role> update(@CurrentUser UserPrincipal userPrincipal,
                                 @PathVariable String id,
                                 @RequestBody UpdateRoleRequest request) {
        String emailUser = userPrincipal.getEmail();
        RoleService.RoleServiceReturn roleServiceReturn = roleService.update(emailUser, id, request);
        return new APIResponse(roleServiceReturn.getData(), roleServiceReturn.getReturnCode(), roleServiceReturn.getMessage());
    }

    @Operation(summary = "Delete a role",
            description = "Delete a role",
            tags = "Role APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Delete role successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))
            ),
            @ApiResponse(responseCode = RoleReturnCode.NOT_FOUND_STRING, description = "Not found role"),
    })
    @DeleteMapping("/")
    public APIResponse<Role> delete(@CurrentUser UserPrincipal userPrincipal,
                                    @RequestBody List<String> ids) {
        String emailUser = userPrincipal.getEmail();
        RoleService.RoleServiceReturn roleServiceReturn = roleService.deleteMultiple(emailUser, ids);
        return new APIResponse(roleServiceReturn.getData(), roleServiceReturn.getReturnCode(), roleServiceReturn.getMessage());
    }
}
