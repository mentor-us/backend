package com.hcmus.mentor.backend.security.oauth2;

import com.hcmus.mentor.backend.config.AppProperties;
import com.hcmus.mentor.backend.exception.BadRequestException;
import com.hcmus.mentor.backend.exception.OAuth2AuthenticationProcessingException;
import com.hcmus.mentor.backend.payload.request.AuthResponse;
import com.hcmus.mentor.backend.payload.returnCode.AuthenticationErrorCode;
import com.hcmus.mentor.backend.security.TokenProvider;
import com.hcmus.mentor.backend.security.UserPrincipal;
import com.hcmus.mentor.backend.service.AuthService;
import com.hcmus.mentor.backend.service.PermissionService;
import com.hcmus.mentor.backend.util.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private static final Logger LOGGER =
      LogManager.getLogger(OAuth2AuthenticationSuccessHandler.class);
  private final AuthService authService;
  private final PermissionService permissionService;
  private TokenProvider tokenProvider;
  private AppProperties appProperties;
  private OAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

  @Autowired
  OAuth2AuthenticationSuccessHandler(
      TokenProvider tokenProvider,
      AppProperties appProperties,
      OAuth2AuthorizationRequestRepository OAuth2AuthorizationRequestRepository,
      AuthService authService,
      PermissionService permissionService) {
    this.tokenProvider = tokenProvider;
    this.appProperties = appProperties;
    this.httpCookieOAuth2AuthorizationRequestRepository = OAuth2AuthorizationRequestRepository;
    this.authService = authService;
    this.permissionService = permissionService;
  }

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {
    String targetUrl = determineTargetUrl(request, response, authentication);

    if (response.isCommitted()) {
      logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
      return;
    }

    clearAuthenticationAttributes(request, response);
    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }

  protected String determineTargetUrl(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    Optional<String> redirectUri =
        CookieUtils.getCookie(
                request, OAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)
            .map(Cookie::getValue);

    if (redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
      throw new BadRequestException(
          "Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication");
    }

    String targetUrl = redirectUri.orElse(getDefaultTargetUrl());
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    if (targetUrl.contains("auth/redirect")
        && !permissionService.isAdmin(
            userPrincipal.getEmail())) { // Check if redirect to Admin Page
      throw new OAuth2AuthenticationProcessingException(AuthenticationErrorCode.UNAUTHORIZED);
    }

    AuthResponse subscription = authService.createToken(userPrincipal.getId());
    return UriComponentsBuilder.fromUriString(targetUrl)
        .queryParam("token", subscription.getAccessToken())
        .queryParam("refreshToken", subscription.getRefreshToken())
        .queryParam("emailVerified", false)
        .build()
        .toUriString();
  }

  protected void clearAuthenticationAttributes(
      HttpServletRequest request, HttpServletResponse response) {
    super.clearAuthenticationAttributes(request);
    httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(
        request, response);
  }

  private boolean isAuthorizedRedirectUri(String uri) {
    URI clientRedirectUri = URI.create(uri);

    return appProperties.getOauth2().getAuthorizedRedirectUris().stream()
        .anyMatch(
            authorizedRedirectUri -> {
              // Only validate host and port. Let the clients use different paths if they want to
              URI authorizedURI = URI.create(authorizedRedirectUri);
              if (authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                  && authorizedURI.getPort() == clientRedirectUri.getPort()) {
                return true;
              }
              return false;
            });
  }
}
