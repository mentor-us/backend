package com.hcmus.mentor.backend.usecase.users.authenticateuser.loginuser;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents user login attempt to system.
 *
 * @see com.hcmus.mentor.backend.usecase.users.authenticateuser.TokenModel
 */
@Getter
@Setter
@Builder
public class LoginUserCommandResult {

  /** Token. */
  private String accessToken;

  /** Token expiration in seconds. */
  private long expiresIn;
}
