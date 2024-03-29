package com.hcmus.mentor.backend.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcmus.mentor.backend.controller.exception.UnauthorizedException;
import com.hcmus.mentor.backend.controller.usecase.user.authenticateuser.AuthenticationTokenService;
import com.hcmus.mentor.backend.security.handler.GlobalControllerExceptionHandler;
import com.hcmus.mentor.backend.security.principal.userdetails.CustomUserDetailService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_SCHEME = "Bearer";
    private final AuthenticationTokenService tokenService;
    private final CustomUserDetailService userDetailsService;
    private final GlobalControllerExceptionHandler exceptionHandler;

//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        return !(new AntPathMatcher().match("/api/auth/**", request.getServletPath()))
//                || !(new AntPathMatcher().match("/api/files/**", request.getServletPath()))
//                || !(new AntPathMatcher().match("/api/**", request.getServletPath()));
//    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        try {
            var authorizationHeader = request.getHeader("Authorization");

            if (!StringUtils.startsWith(authorizationHeader, BEARER_SCHEME)) {
                chain.doFilter(request, response);
                return;
            }

            var token = authorizationHeader.substring(BEARER_SCHEME.length() + 1);

            var claims = getTokenClaims(token);

            var email = getTokenEmail(claims);
            if (StringUtils.isEmpty(email)) {
                chain.doFilter(request, response);
                return;
            }

            var user = userDetailsService.loadUserByUsername(email);
            if (user == null) {
                chain.doFilter(request, response);
                return;
            }

            if (!isTokenValid(claims)) {
                chain.doFilter(request, response);
                return;
            }

            var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception ex) {
            var responseError = exceptionHandler.handlerDomainException(ex);

            String body = new ObjectMapper().writeValueAsString(responseError.getBody());

            response.setContentType("application/json");

            response.setStatus(responseError.getStatusCode().value());
            response.getWriter().write(body);
            response.getWriter().flush();

            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isTokenValid(Claims claims) {
        return !getTokenExpired(claims).before(new Date());
    }

    private Date getTokenExpired(Claims claims) {
        try {
            return claims.getExpiration();
        } catch (Exception ex) {
            throw new UnauthorizedException("Token expired claim cannot be found. Invalid token");
        }
    }

    private String getTokenEmail(Claims claims) {
        try {
            return (String) claims.get("nameidentifier");
        } catch (Exception ex) {
            throw new UnauthorizedException("User identifier claim cannot be found. Invalid token");
        }
    }

    private Claims getTokenClaims(String token) {
        try {
            return tokenService.getTokenClaims(token);
        } catch (Exception ex) {
            throw new UnauthorizedException("Invalid token", ex);
        }
    }
}
