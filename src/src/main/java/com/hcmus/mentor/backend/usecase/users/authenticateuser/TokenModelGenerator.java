package com.hcmus.mentor.backend.usecase.users.authenticateuser;

import java.util.Map;

/** Helper to generate {@link TokenModel}. */
public class TokenModelGenerator {

  /**
   * Common code to generate token and fill with claims.
   *
   * @param authenticationTokenService Authentication token service.
   * @param claims User claims.
   * @return Token model.
   */
  public static TokenModel generate(
      AuthenticationTokenService authenticationTokenService, Map<String, Object> claims) {

    long epoch = System.currentTimeMillis() / 1000;
    claims.put("iat", epoch);

    return TokenModel.builder()
        .accessToken(
            authenticationTokenService.generateToken(
                claims, AuthenticateConstant.accessTokenExpirationTime))
        .expiresIn(AuthenticateConstant.accessTokenExpirationTime.getSeconds())
        .build();
  }
}
