package com.hcmus.mentor.backend.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class RefreshTokenRequest {

    @NotBlank
    private String accessToken;

    @NotBlank
    private String refreshToken;
}
