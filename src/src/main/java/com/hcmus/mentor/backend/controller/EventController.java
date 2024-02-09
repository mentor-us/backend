package com.hcmus.mentor.backend.controller;

import com.hcmus.mentor.backend.controller.payload.ApiResponseDto;
import com.hcmus.mentor.backend.security.CurrentUser;
import com.hcmus.mentor.backend.security.UserPrincipal;
import com.hcmus.mentor.backend.service.EventService;
import com.hcmus.mentor.backend.service.impl.EventServiceImpl;
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

import java.util.Date;
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
     * @param userPrincipal The current user's principal information.
     * @return APIResponse containing a list of own events.
     */
    @GetMapping("own")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<List<EventServiceImpl.Event>> getOwnEvents(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal) {
        return ApiResponseDto.success(eventService.getAllOwnEvents(userPrincipal.getId()));
    }

    /**
     * Retrieves all events owned by the user on a specific date.
     *
     * @param userPrincipal The current user's principal information.
     * @param date          The date for which to retrieve events.
     * @return APIResponse containing a list of own events on the specified date.
     */
    @GetMapping("own/date")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<List<EventServiceImpl.Event>> getAllByDate(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        return ApiResponseDto.success(eventService.getAllEventsByDate(userPrincipal.getId(), date));
    }

    /**
     * Retrieves all events owned by the user in a specific month.
     *
     * @param userPrincipal The current user's principal information.
     * @param date          The date within the desired month.
     * @return APIResponse containing a list of own events in the specified month.
     */
    @GetMapping("own/month")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<List<EventServiceImpl.Event>> getAllByMonth(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date) {
        return ApiResponseDto.success(eventService.getAllEventsByMonth(userPrincipal.getId(), date));
    }
}
