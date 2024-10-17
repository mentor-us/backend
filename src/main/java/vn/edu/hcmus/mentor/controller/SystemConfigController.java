package vn.edu.hcmus.mentor.controller;

import vn.edu.hcmus.mentor.controller.payload.ApiResponseDto;
import vn.edu.hcmus.mentor.domain.SystemConfig;
import vn.edu.hcmus.mentor.security.principal.CurrentUser;
import vn.edu.hcmus.mentor.security.principal.userdetails.CustomerUserDetails;
import vn.edu.hcmus.mentor.service.SystemConfigService;
import vn.edu.hcmus.mentor.service.dto.SystemConfigServiceDto;
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
     * @param customerUserDetails The current user's principal information.
     * @return APIResponse containing the list of all system configurations.
     */
    @GetMapping(value = "all")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<List<SystemConfig>> all(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails) {
        String emailUser = customerUserDetails.getEmail();
        SystemConfigServiceDto configReturn =
                systemConfigService.listAll(emailUser);
        return new ApiResponseDto(
                configReturn.getData(), configReturn.getReturnCode(), configReturn.getMessage());
    }

    /**
     * Update the value of an existing system configuration.
     *
     * @param customerUserDetails The current user's principal information.
     * @param id                  The ID of the system configuration to update.
     * @param value               The new value to set for the system configuration.
     * @return APIResponse containing the updated system configuration or an error response.
     */
    @PatchMapping(value = "{id}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<SystemConfig> updateValue(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable String id,
            @RequestBody Object value) {
        String emailUser = customerUserDetails.getEmail();
        SystemConfigServiceDto configReturn =
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
