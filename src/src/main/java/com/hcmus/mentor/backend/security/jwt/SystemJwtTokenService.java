package com.hcmus.mentor.backend.security.jwt;

import com.hcmus.mentor.backend.security.authenticateuser.AuthenticateConstant;
import com.hcmus.mentor.backend.security.authenticateuser.AuthenticationTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
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
                .setClaims(claims)
                .setExpiration(new Date(new Date().getTime() + expirationTime.toMillis()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Claims getTokenClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = constants.secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
