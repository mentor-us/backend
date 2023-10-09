package com.hcmus.mentor.backend.middleware;

import com.hcmus.mentor.backend.entity.User;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.TokenAuthenticationFilter;
import com.hcmus.mentor.backend.security.TokenProvider;
import com.hcmus.mentor.backend.util.RequestUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class CheckingActivateUserFilter extends OncePerRequestFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(TokenAuthenticationFilter.class);
  private static final String[] AUTH_WHITELIST = {
    // -- Swagger UI v3 (OpenAPI)
    "v3/api-docs",
    "swagger-ui",
    // other public endpoints of your API may be appended to this array
    "favicon.ico",
    "auth/login",
    "auth/refresh-token"
  };
  private final TokenProvider tokenProvider;
  private final UserRepository userRepository;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String jwt = RequestUtils.getJwtFromRequest(request);
      String domain = request.getRequestURL().toString();

      boolean isAllowed = Arrays.stream(AUTH_WHITELIST).anyMatch(domain::contains);
      if (!isAllowed) {
        if (!StringUtils.hasText(jwt) || !tokenProvider.validateToken(jwt)) {
          response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please login!");
          return;
        }

        String userId = tokenProvider.getUserIdFromToken(jwt);
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
          response.sendError(HttpServletResponse.SC_NOT_FOUND, "No account!");
          return;
        }

        if (!userOptional.get().getEmailVerified()) {
          response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please activate account!");
          return;
        }
      }
    } catch (Exception ex) {
      LOGGER.error("Error checking activate user", ex);
    }

    filterChain.doFilter(request, response);
  }
}
