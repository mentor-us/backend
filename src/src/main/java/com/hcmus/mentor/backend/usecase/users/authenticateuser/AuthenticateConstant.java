package com.hcmus.mentor.backend.usecase.users.authenticateuser;

import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Shared constants for authentication. */
@Component
public class AuthenticateConstant {

  /** Secret key for jwt. */
  @Value("${app.jwt.secret-key}")
  public String secretKey;

  @Value("${app.oauth2.authorized-redirect-uris}")
  public List<String> authorizedRedirectUris;

  /** Refresh token expiration time. */
  public static Duration refreshTokenExpire = Duration.ofDays(20);

  /** Access token expiration time. */
  public static Duration accessTokenExpirationTime = Duration.ofHours(3);
}
