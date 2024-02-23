package com.hcmus.mentor.backend.controller;

import com.hcmus.mentor.backend.controller.payload.ApiResponseDto;
import com.hcmus.mentor.backend.controller.payload.request.AddNotificationRequest;
import com.hcmus.mentor.backend.controller.payload.request.SubscribeNotificationRequest;
import com.hcmus.mentor.backend.domain.Notification;
import com.hcmus.mentor.backend.repository.NotificationRepository;
import com.hcmus.mentor.backend.security.principal.CurrentUser;
import com.hcmus.mentor.backend.security.principal.userdetails.CustomerUserDetails;
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
     * @param customerUserDetails The current user's principal information.
     * @param page          The page number (default is 0).
     * @param pageSize      The number of notifications per page (default is 25).
     * @return APIResponse containing the list of notifications for the user.
     */
    @GetMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Map<String, Object>> all(
            @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int pageSize) {
        Map<String, Object> response = notificationService.getOwnNotifications(customerUserDetails.getId(), page, pageSize);
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
    public ApiResponseDto<Notification> get(@PathVariable String id) {
        Optional<Notification> notification = notificationRepository.findById(id);
        return notification.map(ApiResponseDto::success).orElseGet(() -> ApiResponseDto.notFound(NOT_FOUND));
    }

    /**
     * Create a new notification.
     *
     * @param customerUserDetails The current user's principal information.
     * @param request       The request payload for creating a new notification.
     * @return APIResponse containing the created notification or a response indicating failure.
     */
    @PostMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Notification> create(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestBody AddNotificationRequest request) {
        Notification newNotif = notificationService.createResponseNotification(customerUserDetails.getId(), request);
        return ApiResponseDto.success(newNotif);
    }

    /**
     * Respond to a notification (e.g., see, accept, or deny).
     *
     * @param customerUserDetails The current user's principal information.
     * @param id            The ID of the notification to respond to.
     * @param action        The action to perform in response to the notification.
     * @return APIResponse containing the updated notification or a not-found response.
     */
    @PatchMapping("{id}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Notification> response(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable String id,
            @RequestParam String action) {
        String userId = customerUserDetails.getId();
        Notification notif = notificationService.responseNotification(userId, id, action);
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
     * @param customerUserDetails The current user's principal information.
     * @return APIResponse containing the number of unread notifications for the user.
     */
    @GetMapping("unread")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Long> getUnreadNumber(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails) {
        String userId = customerUserDetails.getId();
        if (userId == null) {
            return ApiResponseDto.notFound(404);
        }
        return ApiResponseDto.success(notificationService.getUnreadNumber(userId));
    }
}
