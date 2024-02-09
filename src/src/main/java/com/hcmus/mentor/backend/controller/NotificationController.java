package com.hcmus.mentor.backend.controller;

import com.hcmus.mentor.backend.controller.payload.ApiResponseDto;
import com.hcmus.mentor.backend.controller.payload.request.AddNotificationRequest;
import com.hcmus.mentor.backend.controller.payload.request.SubscribeNotificationRequest;
import com.hcmus.mentor.backend.domain.Notify;
import com.hcmus.mentor.backend.repository.NotificationRepository;
import com.hcmus.mentor.backend.security.CurrentUser;
import com.hcmus.mentor.backend.security.UserPrincipal;
import com.hcmus.mentor.backend.service.NotificationService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

import static com.hcmus.mentor.backend.controller.payload.returnCode.NotificationReturnCode.NOT_FOUND;

/**
 * Notification controller.
 */
@Tag(name = "notifications")
@RestController
@RequestMapping("api/notifications")
@SecurityRequirement(name = "bearer")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    /**
     * Get all notifications with paging.
     *
     * @param userPrincipal The current user's principal information.
     * @param page          The page number (default is 0).
     * @param pageSize      The number of notifications per page (default is 25).
     * @return APIResponse containing the list of notifications for the user.
     */
    @GetMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Map<String, Object>> all(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int pageSize) {
        Map<String, Object> response = notificationService.getOwnNotifications(userPrincipal.getId(), page, pageSize);
        return ApiResponseDto.success(response);
    }

    /**
     * Retrieve an existing notification by ID.
     *
     * @param id The ID of the notification to retrieve.
     * @return APIResponse containing the retrieved notification or a not-found response.
     */
    @GetMapping("{id}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Notify> get(@PathVariable String id) {
        Optional<Notify> notification = notificationRepository.findById(id);
        return notification.map(ApiResponseDto::success).orElseGet(() -> ApiResponseDto.notFound(NOT_FOUND));
    }

    /**
     * Create a new notification.
     *
     * @param userPrincipal The current user's principal information.
     * @param request       The request payload for creating a new notification.
     * @return APIResponse containing the created notification or a response indicating failure.
     */
    @PostMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Notify> create(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @RequestBody AddNotificationRequest request) {
        Notify newNotif = notificationService.createResponseNotification(userPrincipal.getId(), request);
        return ApiResponseDto.success(newNotif);
    }

    /**
     * Respond to a notification (e.g., see, accept, or deny).
     *
     * @param userPrincipal The current user's principal information.
     * @param id            The ID of the notification to respond to.
     * @param action        The action to perform in response to the notification.
     * @return APIResponse containing the updated notification or a not-found response.
     */
    @PatchMapping("{id}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Notify> response(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable String id,
            @RequestParam String action) {
        String userId = userPrincipal.getId();
        Notify notif = notificationService.responseNotification(userId, id, action);
        if (notif == null) {
            return ApiResponseDto.notFound(NOT_FOUND);
        }
        return ApiResponseDto.success(notif);
    }

    /**
     * Subscribe to the Notification Server.
     *
     * @param request The request payload containing registration token for subscribing to notifications.
     * @return APIResponse indicating success or failure of subscription.
     */
    @PostMapping("subscribe")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Void> subscribe(@RequestBody SubscribeNotificationRequest request) {
        notificationService.subscribeNotification(request);
        return ApiResponseDto.success(null);
    }

    /**
     * Subscribe to a specific topic.
     *
     * @param topic The topic to subscribe to.
     * @param token The registration token for subscribing to the topic.
     * @return APIResponse indicating success or failure of topic subscription.
     */
    @PostMapping("subscribe/{topic}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Void> subscribeTopic(@RequestParam String topic, @PathVariable String token) {
        return ApiResponseDto.notFound(404);
    }

    /**
     * Get the number of unread notifications.
     *
     * @param userPrincipal The current user's principal information.
     * @return APIResponse containing the number of unread notifications for the user.
     */
    @GetMapping("unread")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Long> getUnreadNumber(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal) {
        String userId = userPrincipal.getId();
        if (userId == null) {
            return ApiResponseDto.notFound(404);
        }
        return ApiResponseDto.success(notificationService.getUnreadNumber(userId));
    }
}
