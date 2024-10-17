package vn.edu.hcmus.mentor.backend.controller.usecase.user.authenticateuser.refreshtoken;

import an.awesome.pipelinr.Command;
import vn.edu.hcmus.mentor.backend.controller.usecase.user.authenticateuser.TokenModel;
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
