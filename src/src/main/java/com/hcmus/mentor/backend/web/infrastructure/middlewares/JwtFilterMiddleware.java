package com.hcmus.mentor.backend.web.infrastructure.middlewares;

import com.hcmus.mentor.backend.exception.DomainException;
import com.hcmus.mentor.backend.web.infrastructure.security.CustomUserDetailService;
import com.hcmus.mentor.backend.usecase.users.authenticateuser.AuthenticationTokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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
public class JwtFilterMiddleware extends OncePerRequestFilter {

  private final AuthenticationTokenService tokenService;
  private final CustomUserDetailService userDetailsService;

  private static final String scheme = "Bearer";

  @SneakyThrows
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    var authorizationHeader = request.getHeader("Authorization");

    if (StringUtils.startsWith(authorizationHeader, scheme)) {
      var token = authorizationHeader.substring("Bearer".length() + 1);
      var email = getTokenEmail(token);

      var userDetails = userDetailsService.loadUserByUsername(email);
      if (userDetails != null) {
        var authentication =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
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
