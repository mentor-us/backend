package com.hcmus.mentor.backend.usecase.users.authenticateuser.refreshtoken;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.usecase.users.authenticateuser.TokenModel;
import lombok.Getter;
import lombok.Setter;

/** Refresh token command. */
@Getter
@Setter
public class RefreshTokenCommand implements Command<TokenModel> {

  /** User token. */
  private String token;
}
