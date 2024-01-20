package com.hcmus.mentor.backend.security.authenticateuser.refreshtoken;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.security.authenticateuser.TokenModel;
import lombok.Getter;
import lombok.Setter;

/**
 * Refresh token command.
 */
@Getter
@Setter
public class RefreshTokenCommand implements Command<TokenModel> {

    /**
     * User token.
     */
    private String token;
}
