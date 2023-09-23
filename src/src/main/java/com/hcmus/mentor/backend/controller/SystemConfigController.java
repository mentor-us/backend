package com.hcmus.mentor.backend.controller;

import com.hcmus.mentor.backend.entity.SystemConfig;
import com.hcmus.mentor.backend.payload.APIResponse;
import com.hcmus.mentor.backend.payload.returnCode.SystemConfigReturnCode;
import com.hcmus.mentor.backend.security.CurrentUser;
import com.hcmus.mentor.backend.security.UserPrincipal;
import com.hcmus.mentor.backend.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "SystemConfig APIs", description = "REST APIs for SystemConfig collections")
@RestController
@RequestMapping("/api/system-config")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    public SystemConfigController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    @Operation(summary = "Retrieve all system config",
            description = "Retrieve all user information on system",
            tags = "SystemConfig APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retrieve successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            )})
    @GetMapping(value = "/all")
    public APIResponse<List<SystemConfig>> all(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal) {
        String emailUser = userPrincipal.getEmail();
        SystemConfigService.SystemConfigReturnService configReturn = systemConfigService.listAll(emailUser);
        return new APIResponse(configReturn.getData(), configReturn.getReturnCode(), configReturn.getMessage());
    }

    @Operation(summary = "Update value of an existing system config",
            description = "Update value of an existing system config",
            tags = "SystemConfig APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retrieve successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
            @ApiResponse(responseCode = SystemConfigReturnCode.NOT_FOUND_STRING, description = "Not found system config"),
            @ApiResponse(responseCode = SystemConfigReturnCode.INVALID_TYPE_STRING, description = "Invalid type"),
            @ApiResponse(responseCode = SystemConfigReturnCode.INVALID_DOMAIN_STRING, description = "Invalid domain"),
            @ApiResponse(responseCode = SystemConfigReturnCode.INVALID_MAX_YEAR_STRING, description = "Invalid max year")


    })
    @PatchMapping(value = "/{id}")
    public APIResponse<SystemConfig> updateValue(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                                 @PathVariable String id,
                                                 @RequestBody Object value) {
        String emailUser = userPrincipal.getEmail();
        SystemConfigService.SystemConfigReturnService configReturn = systemConfigService.updateValue(emailUser, id, value);
        return new APIResponse(configReturn.getData(), configReturn.getReturnCode(), configReturn.getMessage());
    }

    @Operation(summary = "Endpoint warmup database",
            description = "Used to automatically insert data to database",
            tags = "SystemConfig APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Warmup successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            ),
    })
    @PostMapping(value = {"/", ""})
    public void add() {
        systemConfigService.add();
    }

}
