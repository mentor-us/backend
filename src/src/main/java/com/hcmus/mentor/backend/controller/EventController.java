package com.hcmus.mentor.backend.controller;

import com.hcmus.mentor.backend.payload.APIResponse;
import com.hcmus.mentor.backend.security.CurrentUser;
import com.hcmus.mentor.backend.security.UserPrincipal;
import com.hcmus.mentor.backend.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Date;
import java.util.List;

@Tag(name = "Event APIs", description = "REST APIs for user event")
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @Operation(summary = "Get all own events",
            description = "API to get all own events (meeting, task) of user on Calendar", tags = "Event APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            )})
    @GetMapping("/own")
    public APIResponse<List<EventService.Event>> getOwnEvents(@ApiIgnore @CurrentUser UserPrincipal userPrincipal) {
        return APIResponse.success(eventService.getAllOwnEvents(userPrincipal.getId()));
    }

    @Operation(summary = "Get all own events by date",
            description = "API to get all own events in one day", tags = "Event APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            )})
    @GetMapping("/own/date")
    public APIResponse<List<EventService.Event>> getAllByDate(@ApiIgnore @CurrentUser UserPrincipal userPrincipal,
                                                              @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        return APIResponse.success(eventService.getAllEventsByDate(userPrincipal.getId(), date));
    }

    @Operation(summary = "Get all own events by month",
            description = "API to get all own events in one month", tags = "Event APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))
            )})
    @GetMapping("/own/month")
    public APIResponse<List<EventService.Event>> getAllByMonth(@ApiIgnore @CurrentUser UserPrincipal userPrincipal,
                                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date) {
        return APIResponse.success(eventService.getAllEventsByMonth(userPrincipal.getId(), date));
    }
}
