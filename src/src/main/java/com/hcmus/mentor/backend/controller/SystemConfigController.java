package com.hcmus.mentor.backend.controller;

import com.hcmus.mentor.backend.controller.payload.ApiResponseDto;
import com.hcmus.mentor.backend.domain.SystemConfig;
import com.hcmus.mentor.backend.security.CurrentUser;
import com.hcmus.mentor.backend.security.UserPrincipal;
import com.hcmus.mentor.backend.service.SystemConfigReturnService;
import com.hcmus.mentor.backend.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * System config controller.
 */
@Tag(name = "system config")
@RestController
@RequestMapping("api/system-config")
@SecurityRequirement(name = "bearer")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    /**
     * Retrieve all system configurations.
     *
     * @param userPrincipal The current user's principal information.
     * @return APIResponse containing the list of all system configurations.
     */
    @GetMapping(value = "all")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<List<SystemConfig>> all(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal) {
        String emailUser = userPrincipal.getEmail();
        SystemConfigReturnService configReturn =
                systemConfigService.listAll(emailUser);
        return new ApiResponseDto(
                configReturn.getData(), configReturn.getReturnCode(), configReturn.getMessage());
    }

    /**
     * Update the value of an existing system configuration.
     *
     * @param userPrincipal The current user's principal information.
     * @param id            The ID of the system configuration to update.
     * @param value         The new value to set for the system configuration.
     * @return APIResponse containing the updated system configuration or an error response.
     */
    @PatchMapping(value = "{id}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<SystemConfig> updateValue(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable String id,
            @RequestBody Object value) {
        String emailUser = userPrincipal.getEmail();
        SystemConfigReturnService configReturn =
                systemConfigService.updateValue(emailUser, id, value);
        return new ApiResponseDto(
                configReturn.getData(), configReturn.getReturnCode(), configReturn.getMessage());
    }

    /**
     * Warm up the database by automatically inserting data.
     */
    @PostMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public void add() {
        systemConfigService.add();
    }
}
