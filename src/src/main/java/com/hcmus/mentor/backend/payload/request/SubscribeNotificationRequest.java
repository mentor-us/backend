package com.hcmus.mentor.backend.payload.request;

import lombok.*;

import javax.validation.constraints.NotEmpty;

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
