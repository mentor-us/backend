package com.hcmus.mentor.backend.controller;

import com.hcmus.mentor.backend.payload.APIResponse;
import com.hcmus.mentor.backend.security.CurrentUser;
import com.hcmus.mentor.backend.security.UserPrincipal;
import com.hcmus.mentor.backend.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Date;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Event APIs", description = "REST APIs for user event")
@RestController
@RequestMapping("/api/events")
@SecurityRequirement(name = "bearer")
public class EventController {

  private final EventService eventService;

  public EventController(EventService eventService) {
    this.eventService = eventService;
  }

  @Operation(
      summary = "Get all own events",
      description = "API to get all own events (meeting, task) of user on Calendar",
      tags = "Event APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Get successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class))))
  })
  @GetMapping("/own")
  public APIResponse<List<EventService.Event>> getOwnEvents(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal) {
    return APIResponse.success(eventService.getAllOwnEvents(userPrincipal.getId()));
  }

  @Operation(
      summary = "Get all own events by date",
      description = "API to get all own events in one day",
      tags = "Event APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Get successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class))))
  })
  @GetMapping("/own/date")
  public APIResponse<List<EventService.Event>> getAllByDate(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
    return APIResponse.success(eventService.getAllEventsByDate(userPrincipal.getId(), date));
  }

  @Operation(
      summary = "Get all own events by month",
      description = "API to get all own events in one month",
      tags = "Event APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Get successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class))))
  })
  @GetMapping("/own/month")
  public APIResponse<List<EventService.Event>> getAllByMonth(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date) {
    return APIResponse.success(eventService.getAllEventsByMonth(userPrincipal.getId(), date));
  }
}
