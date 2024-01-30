package com.hcmus.mentor.backend.security.authenticateuser;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * API generated token model.
 */
@Getter
@Setter
@Builder
public class TokenModel {

    /**
     * Token.
     */
    private String accessToken;

    /**
     * Token expiration in seconds.
     */
    private long expiresIn;
}
