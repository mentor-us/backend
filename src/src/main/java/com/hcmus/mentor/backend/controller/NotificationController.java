package com.hcmus.mentor.backend.controller;

import com.hcmus.mentor.backend.controller.payload.ApiResponseDto;
import com.hcmus.mentor.backend.controller.payload.request.notifications.AddNotificationRequest;
import com.hcmus.mentor.backend.controller.payload.request.notifications.SubscribeNotificationServerRequest;
import com.hcmus.mentor.backend.controller.usecase.notification.common.NotificationDetailDto;
import com.hcmus.mentor.backend.domain.Notification;
import com.hcmus.mentor.backend.domain.constant.NotificationAction;
import com.hcmus.mentor.backend.service.NotificationService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    /**
     * Get all notifications with paging.
     *
     * @param page     The page number (default is 0).
     * @param pageSize The number of notifications per page (default is 25).
     * @return APIResponse containing the list of notifications for the user.
     */
    @GetMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public Map<String, Object> all(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int pageSize) {
        return notificationService.getOwn(page, pageSize);
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
    public NotificationDetailDto get(@PathVariable String id) {
        return notificationService.getById(id);
    }

    /**
     * Create a new notification.
     *
     * @param request The request payload for creating a new notification.
     * @return APIResponse containing the created notification or a response indicating failure.
     */
    @PostMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public Notification create(
            @RequestBody AddNotificationRequest request) {
        return notificationService.create(request);
    }

    /**
     * Respond to a notification (e.g., see, accept, or deny).
     *
     * @param id     The ID of the notification to respond to.
     * @param action The action to perform in response to the notification.
     * @return APIResponse containing the updated notification or a not-found response.
     */
    @PatchMapping("{id}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public Notification response(
            @PathVariable String id,
            @RequestParam NotificationAction action) {
        return notificationService.response(id, action);
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
    public ResponseEntity<Void> subscribe(@RequestBody SubscribeNotificationServerRequest request) {
        notificationService.subscribeToServer(request);

        return ResponseEntity.ok().build();
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
     * @return APIResponse containing the number of unread notifications for the user.
     */
    @GetMapping("unread")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public Long getUnreadNumber() {
        return notificationService.getCountUnread();
    }
}