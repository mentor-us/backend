package com.hcmus.mentor.backend.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcmus.mentor.backend.controller.exception.NotFoundException;
import com.hcmus.mentor.backend.controller.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * Always returns a 403 error code to the client.
 */
@RequiredArgsConstructor
public class CustomHttp403UnauthorizedEntryPoint implements AuthenticationEntryPoint {
    private static final String BEARER_SCHEME = "Bearer";
    private static final Log logger = LogFactory.getLog(CustomHttp403UnauthorizedEntryPoint.class);
    private final GlobalControllerExceptionHandler exceptionHandler;

    /**
     * Always returns a 401 error code to the client.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        logger.debug("Pre-authenticated entry point called. Rejecting access");

        var authorizationHeader = request.getHeader("Authorization");

        if (!StringUtils.startsWith(authorizationHeader, BEARER_SCHEME)) {
            handleUnauthorizedRequest(response);

            return;
        }

        handleNotFoundRequest(response);
    }

    private void handleUnauthorizedRequest(HttpServletResponse response) throws IOException {
        var responseError = exceptionHandler.handlerDomainException(new UnauthorizedException("Not found Bearer scheme in Authorization header"));

        String body = new ObjectMapper().writeValueAsString(responseError.getBody());

        response.setContentType("application/json");

        response.setStatus(responseError.getStatusCode().value());
        response.getWriter().write(body);
        response.getWriter().flush();
    }

    private void handleNotFoundRequest(HttpServletResponse response) throws IOException {
        var responseError = exceptionHandler.handlerDomainException(new NotFoundException("Route not found"));

        String body = new ObjectMapper().writeValueAsString(responseError.getBody());

        response.setContentType("application/json");

        response.setStatus(responseError.getStatusCode().value());
        response.getWriter().write(body);
        response.getWriter().flush();
    }
}
