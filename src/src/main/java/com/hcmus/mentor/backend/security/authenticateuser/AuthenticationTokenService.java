package com.hcmus.mentor.backend.security.authenticateuser;

import io.jsonwebtoken.Claims;

import java.time.Duration;
import java.util.Map;

/**
 * Methods to help generate and parse authentication token.
 */
public interface AuthenticationTokenService {

    /**
     * Generate access token.
     *
     * @param claims         User claims.
     * @param expirationTime Token expiration time.
     * @return Token.
     */
    String generateToken(Map<String, Object> claims, Duration expirationTime);

    /**
     * Get token claims.
     *
     * @param token User token.
     * @return User claims.
     */
    Claims getTokenClaims(String token);
}
