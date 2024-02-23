package com.hcmus.mentor.backend.controller.usecase.user.authenticateuser;

import java.util.Map;

/**
 * Helper to generate {@link TokenModel}.
 */
public class TokenModelGenerator {

    private TokenModelGenerator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Common code to generate token and fill with claims.
     *
     * @param authenticationTokenService Authentication token service.
     * @param claims                     User claims.
     * @return Token model.
     */
    public static TokenModel generate(
            AuthenticationTokenService authenticationTokenService, Map<String, Object> claims) {

        long epoch = System.currentTimeMillis() / 1000;
        claims.put("iat", epoch);

        var accessToken = authenticationTokenService.generateToken(claims, AuthenticateConstant.ACCESS_TOKEN_EXPIRATION_TIME);

        return TokenModel.builder()
                .accessToken(accessToken)
                .expiresIn(AuthenticateConstant.ACCESS_TOKEN_EXPIRATION_TIME.getSeconds())
                .build();
    }
}
