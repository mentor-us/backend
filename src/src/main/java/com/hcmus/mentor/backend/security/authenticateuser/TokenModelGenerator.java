package com.hcmus.mentor.backend.security.authenticateuser;

import java.util.Map;

/**
 * Helper to generate {@link TokenModel}.
 */
public class TokenModelGenerator {

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

        var accessToken = authenticationTokenService.generateToken(claims, AuthenticateConstant.accessTokenExpirationTime);

        return TokenModel.builder()
                .accessToken(accessToken)
                .expiresIn(AuthenticateConstant.accessTokenExpirationTime.getSeconds())
                .build();
    }
}
