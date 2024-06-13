package com.hcmus.mentor.backend.controller.payload.request.notifications;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscribeNotificationServerRequest {

    @NotEmpty
    private String userId;

    @NotEmpty
    private String token;
}