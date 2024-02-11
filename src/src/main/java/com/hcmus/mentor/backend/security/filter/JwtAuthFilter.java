package com.hcmus.mentor.backend.security.filter;

import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.security.principal.userdetails.CustomUserDetailService;
import com.hcmus.mentor.backend.controller.usecase.user.authenticateuser.AuthenticationTokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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

        if (StringUtils.startsWith(authorizationHeader, BEARER_SCHEME)) {
            var token = authorizationHeader.substring(BEARER_SCHEME.length() + 1);
            var email = getTokenEmail(token);

            var userDetails = userDetailsService.loadUserByUsername(email);
            if (userDetails != null) {
                var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        chain.doFilter(request, response);
    }

    private String getTokenEmail(String token) throws DomainException {
        try {
            Claims claims = getTokenClaims(token);

            return (String) claims.get("nameidentifier");
        } catch (Exception ex) {
            throw new DomainException("User identifier claim cannot be found. Invalid token");
        }
    }

    private Claims getTokenClaims(String token) throws DomainException {
        try {
            return tokenService.getTokenClaims(token);
        } catch (Exception ex) {
            throw new DomainException("Invalid token.");
        }
    }
}
