package com.hcmus.mentor.backend.controller;

import com.hcmus.mentor.backend.controller.payload.ApiResponseDto;
import com.hcmus.mentor.backend.security.principal.CurrentUser;
import com.hcmus.mentor.backend.security.principal.userdetails.CustomerUserDetails;
import com.hcmus.mentor.backend.service.EventService;
import com.hcmus.mentor.backend.service.dto.EventDto;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Event controller.
 */
@Tag(name = "events")
@RestController
@RequestMapping("api/events")
@SecurityRequirement(name = "bearer")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /**
     * Retrieves all events owned by the user.
     *
     * @param customerUserDetails The current user's principal information.
     * @return APIResponse containing a list of own events.
     */
    @GetMapping("own")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<List<EventDto>> getOwnEvents(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails) {
        return ApiResponseDto.success(eventService.getAllOwnEvents(customerUserDetails.getId()));
    }

    /**
     * Retrieves all events owned by the user on a specific date.
     *
     * @param customerUserDetails The current user's principal information.
     * @param date                The date for which to retrieve events.
     * @return APIResponse containing a list of own events on the specified date.
     */
    @GetMapping("own/date")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<List<EventDto>> getAllByDate(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDateTime date) {
        return ApiResponseDto.success(eventService.getAllEventsByDate(customerUserDetails.getId(), date));
    }

    /**
     * Retrieves all events owned by the user in a specific month.
     *
     * @param customerUserDetails The current user's principal information.
     * @param date                The date within the desired month.
     * @return APIResponse containing a list of own events in the specified month.
     */
    @GetMapping("own/month")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<List<EventDto>> getAllByMonth(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime date) {
        return ApiResponseDto.success(eventService.getAllEventsByMonth(customerUserDetails.getId(), date));
    }
}
