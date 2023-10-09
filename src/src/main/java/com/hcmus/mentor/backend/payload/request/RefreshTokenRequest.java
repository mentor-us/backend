package com.hcmus.mentor.backend.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class RefreshTokenRequest {

  @NotBlank private String accessToken;

  @NotBlank private String refreshToken;
}
