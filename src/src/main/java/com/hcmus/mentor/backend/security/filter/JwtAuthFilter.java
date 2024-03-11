package com.hcmus.mentor.backend.security.filter;

import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.usecase.user.authenticateuser.AuthenticationTokenService;
import com.hcmus.mentor.backend.security.principal.userdetails.CustomUserDetailService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_SCHEME = "Bearer";
    private final AuthenticationTokenService tokenService;
    private final CustomUserDetailService userDetailsService;

    @SneakyThrows
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) {
        var authorizationHeader = request.getHeader("Authorization");

        if (!StringUtils.startsWith(authorizationHeader, BEARER_SCHEME)) {
            chain.doFilter(request, response);
            return;
        }

        var token = authorizationHeader.substring(BEARER_SCHEME.length() + 1);
        var email = getTokenEmail(token);

        if (StringUtils.isEmpty(email)) {
            chain.doFilter(request, response);
            return;
        }

        var user = userDetailsService.loadUserByUsername(email);
        if (user == null) {
            chain.doFilter(request, response);
            return;
        }

        if (!isTokenValid(token, user)) {
            chain.doFilter(request, response);
            return;
        }

        var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        chain.doFilter(request, response);
    }

    private boolean isTokenValid(String token, UserDetails user) {
        return !getTokenExpired(token).before(new Date());
    }

    private Date getTokenExpired(String token) {
        try {
            Claims claims = getTokenClaims(token);

            return claims.getExpiration();
        } catch (Exception ex) {
            throw new DomainException("Token expired claim cannot be found. Invalid token");
        }
    }

    private String getTokenEmail(String token) {
        try {
            Claims claims = getTokenClaims(token);

            return (String) claims.get("nameidentifier");
        } catch (Exception ex) {
            throw new DomainException("User identifier claim cannot be found. Invalid token");
        }
    }

    private Claims getTokenClaims(String token) {
        try {
            return tokenService.getTokenClaims(token);
        } catch (Exception ex) {
            throw new DomainException("Invalid token.");
        }
    }
}
