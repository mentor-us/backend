package com.hcmus.mentor.backend.security.handler;

import com.hcmus.mentor.backend.controller.exception.BadRequestException;
import com.hcmus.mentor.backend.controller.usecase.user.authenticateuser.AuthenticateConstant;
import com.hcmus.mentor.backend.controller.usecase.user.authenticateuser.AuthenticationTokenService;
import com.hcmus.mentor.backend.controller.usecase.user.authenticateuser.TokenModelGenerator;
import com.hcmus.mentor.backend.security.principal.oauth2.OAuth2AuthorizationRequestRepository;
import com.hcmus.mentor.backend.security.principal.userdetails.CustomerUserDetails;
import com.hcmus.mentor.backend.service.PermissionService;
import com.hcmus.mentor.backend.util.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger LOGGER = LogManager.getLogger(OAuth2AuthenticationSuccessHandler.class);
    private final PermissionService permissionService;
    private final OAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    private final AuthenticationTokenService tokenService;
    private final AuthenticateConstant authenticateConstant;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException {
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    @Override
    protected String determineTargetUrl(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) {
        Optional<String> redirectUri = CookieUtils.getCookie(request, OAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        if (redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
            throw new BadRequestException("Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication");
        }

        String targetUrl = redirectUri.orElse(getDefaultTargetUrl());
        CustomerUserDetails customerUserDetails = (CustomerUserDetails) authentication.getPrincipal();

        var claims = new HashMap<String, Object>();
        claims.put("sub", customerUserDetails.getId());
        claims.put("nameidentifier", customerUserDetails.getEmail());
        claims.put("name", customerUserDetails.getEmail());
        claims.put("emailaddress", customerUserDetails.getEmail());

        var token = TokenModelGenerator.generate(tokenService, claims);

        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("token", token.getAccessToken())
                .queryParam("emailVerified", false)
                .queryParam("expiresIn", token.getExpiresIn())
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

        return authenticateConstant.authorizedRedirectUris.stream()
                .anyMatch(authorizedRedirectUri -> {
                    // Only validate host and port. Let the clients use different paths if they want to
                    URI authorizedURI = URI.create(authorizedRedirectUri);
                    return authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                            && authorizedURI.getPort() == clientRedirectUri.getPort();
                });
    }
}
