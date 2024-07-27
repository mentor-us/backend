package com.hcmus.mentor.backend.controller.usecase.user.authenticateuser;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * Shared constants for authentication.
 */
@Component
public class AuthenticateConstant {

    /**
     * Refresh token expiration time.
     */
    public static final Duration REFRESH_TOKEN_EXPIRE = Duration.ofDays(20);

    /**
     * Access token expiration time.
     */
    public static final Duration ACCESS_TOKEN_EXPIRATION_TIME = Duration.ofDays(30);

    /**
     * Issuer for jwt.
     */
    @Value("${app.jwt.issuer}")
    public String issuer;

    /**
     * Secret key for jwt.
     */
    @Value("${app.jwt.secret-key}")
    public String secretKey;

    /**
     * List of authorized redirect URIs for OAuth2.
     */
    @Value("${app.oauth2.authorized-redirect-uris}")
    public List<String> authorizedRedirectUris;
}
