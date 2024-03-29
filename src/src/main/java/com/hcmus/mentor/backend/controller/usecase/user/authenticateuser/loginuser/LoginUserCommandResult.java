package com.hcmus.mentor.backend.controller.usecase.user.authenticateuser.loginuser;

import com.hcmus.mentor.backend.controller.usecase.user.authenticateuser.TokenModel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents user login attempt to system.
 *
 * @see TokenModel
 */
@Getter
@Setter
@Builder
public class LoginUserCommandResult {

    /**
     * Token.
     */
    private String accessToken;

    /**
     * Token expiration in seconds.
     */
    private long expiresIn;
}
