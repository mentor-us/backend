package com.hcmus.mentor.backend.controller.usecase.user.authenticateuser.loginuser;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

/**
 * Command to log in user by password.
 */
@Getter
@Setter
public class LoginUserCommand implements Command<LoginUserCommandResult> {

    /**
     * Email of user account.
     */
    @Email(message = "It not email")
    private String email;

    /**
     * Password of account user.
     */
    private String password;

    /**
     * Remember user's cookie for longer period.
     */
    private Boolean rememberMe = false;
}
