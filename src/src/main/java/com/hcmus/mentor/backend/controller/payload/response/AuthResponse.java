package com.hcmus.mentor.backend.controller.payload.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {

    private String refreshToken;

    private String accessToken;

    @Builder.Default
    private String tokenType = "Bearer";
}