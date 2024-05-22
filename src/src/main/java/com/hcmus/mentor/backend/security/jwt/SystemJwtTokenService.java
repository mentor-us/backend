package com.hcmus.mentor.backend.security.jwt;

import com.hcmus.mentor.backend.controller.usecase.user.authenticateuser.AuthenticateConstant;
import com.hcmus.mentor.backend.controller.usecase.user.authenticateuser.AuthenticationTokenService;
import com.hcmus.mentor.backend.util.DateUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

/**
 * {@inheritDoc}
 */
@Service
@RequiredArgsConstructor
public class SystemJwtTokenService implements AuthenticationTokenService {

    private final AuthenticateConstant constants;

    /**
     * {@inheritDoc}
     */
    @Override
    public String generateToken(Map<String, Object> claims, Duration expirationTime) {
        return Jwts.builder()
                .header()
                .type("JWT")
                .and()
                .claims(claims)
                .issuer(constants.issuer)
                .issuedAt(DateUtils.getCurrentDateAtUTC() )
                .expiration(new Date(DateUtils.getCurrentDateAtUTC() .getTime() + expirationTime.toMillis()))
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Claims getTokenClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(constants.secretKey.getBytes(StandardCharsets.UTF_8));
    }
}
