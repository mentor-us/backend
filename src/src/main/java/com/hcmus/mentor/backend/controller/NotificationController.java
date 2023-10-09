package com.hcmus.mentor.backend.controller;

import static com.hcmus.mentor.backend.payload.returnCode.NotificationReturnCode.NOT_FOUND;

import com.hcmus.mentor.backend.entity.Notif;
import com.hcmus.mentor.backend.payload.APIResponse;
import com.hcmus.mentor.backend.payload.request.AddNotificationRequest;
import com.hcmus.mentor.backend.payload.request.SubscribeNotificationRequest;
import com.hcmus.mentor.backend.repository.NotificationRepository;
import com.hcmus.mentor.backend.security.CurrentUser;
import com.hcmus.mentor.backend.security.UserPrincipal;
import com.hcmus.mentor.backend.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import java.util.Optional;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notification APIs", description = "REST APIs for Notification collection")
@RestController
@RequestMapping("/api/notifications")
@SecurityRequirement(name = "bearer")
public class NotificationController {

  private final NotificationService notificationService;

  private final NotificationRepository notificationRepository;

  public NotificationController(
      NotificationService notificationService, NotificationRepository notificationRepository) {
    this.notificationService = notificationService;
    this.notificationRepository = notificationRepository;
  }

  @Operation(
      summary = "Get all notifications (Paging)",
      description = "",
      tags = "Notification APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Get successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication")
  })
  @GetMapping(value = {"", "/"})
  public APIResponse all(
      @CurrentUser UserPrincipal userPrincipal,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "25") int pageSize) {
    Map<String, Object> response =
        notificationService.getOwnNotifications(userPrincipal.getId(), page, pageSize);
    return APIResponse.success(response);
  }

  @Operation(
      summary = "Retrieve existing notification",
      description = "",
      tags = "Notification APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Get successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication")
  })
  @GetMapping("/{id}")
  public APIResponse<Notif> get(@PathVariable String id) {
    Optional<Notif> notification = notificationRepository.findById(id);
    return notification.map(APIResponse::success).orElseGet(() -> APIResponse.notFound(NOT_FOUND));
  }

  @Operation(summary = "Update existing notification", description = "", tags = "Notification APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Update successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication")
  })
  @PostMapping(value = {"", "/"})
  public APIResponse<Notif> create(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @RequestBody AddNotificationRequest request) {
    Notif newNotif = notificationService.createResponseNotification(userPrincipal.getId(), request);
    return APIResponse.success(newNotif);
  }

  @Operation(
      summary = "Reply notification",
      description =
          "The receiver of notification have to reply to this one (If need)\n"
              + "They can see, accept or deny the notification",
      tags = "Notification APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Reply successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication")
  })
  @PatchMapping("/{id}")
  public APIResponse<Notif> response(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @PathVariable String id,
      @RequestParam String action) {
    String userId = userPrincipal.getId();
    Notif notif = notificationService.responseNotification(userId, id, action);
    if (notif == null) {
      return APIResponse.notFound(NOT_FOUND);
    }
    return APIResponse.success(notif);
  }

  @Operation(
      summary = "Mobile app subscribe to Notification Server",
      description = "Send registration token to server",
      tags = "Notification APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Reply successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication")
  })
  @PostMapping("/subscribe")
  public APIResponse<Void> subscribe(@RequestBody SubscribeNotificationRequest request) {
    notificationService.subscribeNotification(request);
    return APIResponse.success(null);
  }

  @Operation(
      summary = "Mobile app subscribe to topic",
      description = "Send registration token and subscribe topic on server",
      tags = "Notification APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Reply successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication")
  })
  @PostMapping("/subscribe/{topic}")
  public APIResponse<Void> subscribeTopic(@RequestParam String topic, @PathVariable String token) {
    return APIResponse.notFound(404);
  }

  @Operation(
      summary = "Get unread notifications number",
      description = "Get the number of unread notification on user's mobile app",
      tags = "Notification APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Get successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication")
  })
  @GetMapping("/unread")
  public APIResponse<Long> getUnreadNumber(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal) {
    String userId = userPrincipal.getId();
    if (userId == null) {
      return APIResponse.notFound(404);
    }
    return APIResponse.success(notificationService.getUnreadNumber(userId));
  }
}
