package com.hcmus.mentor.backend.controller.payload.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Builder
public class SubscribeNotificationRequest {

    @NotEmpty
    private String userId;

    private String token;
}
