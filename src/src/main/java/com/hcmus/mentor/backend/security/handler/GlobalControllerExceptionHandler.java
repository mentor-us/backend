package com.hcmus.mentor.backend.security.handler;

import com.hcmus.mentor.backend.controller.exception.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;

import static java.util.Map.entry;

/**
 * Exception handling middleware.
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerExceptionHandler {

    private static final String ERRORS_KEY = "errors";
    private static final String CODE_KEY = "code";
    private static final Logger logger = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);
    private static final Map<Class, HttpStatus> exceptionStatusCodes =
            Map.ofEntries(
                    entry(DomainException.class, HttpStatus.BAD_REQUEST),
                    entry(UnauthorizedException.class, HttpStatus.UNAUTHORIZED),
                    entry(ForbiddenException.class, HttpStatus.FORBIDDEN),
                    entry(NotFoundException.class, HttpStatus.NOT_FOUND)
            );
    private final Environment environment;

    private static void addExceptionInfoToProblemDetails(
            ProblemDetail problemDetail, DomainException exception) {
        problemDetail.setTitle(exception.getMessage());
        problemDetail.setType(URI.create(exception.getClass().getSimpleName()));
        addCodeToProblemDetails(problemDetail, exception.getCode());
    }

    private static void addCodeToProblemDetails(ProblemDetail problemDetail, String code) {
        if (code != null && !code.isEmpty()) {
            problemDetail.setProperty(CODE_KEY, code);
        }
    }

    private static HttpStatus getStatusCodeByExceptionType(Class exceptionType) {
        if (exceptionType == DomainException.class) {
            return HttpStatus.BAD_REQUEST;
        }
        for (Map.Entry<Class, HttpStatus> entry : exceptionStatusCodes.entrySet()) {
            if (entry.getKey() == exceptionType) {
                return entry.getValue();
            }
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * Global exception handler.
     *
     * @param ex exception.
     * @return The {@link ProblemDetail}.
     */
    @ExceptionHandler(
            {Exception.class}
    )
    public ResponseEntity<ProblemDetail> handlerDomainException(Exception ex) {
        return new ResponseEntity<>(getObjectByException(ex), getStatusCodeByExceptionType(ex.getClass()));
    }

    private ProblemDetail getObjectByException(Exception exception) {
        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        HttpStatus statusCode = HttpStatus.BAD_REQUEST;

        if (exception instanceof ValidationException validationException) {
            problem.setProperty(
                    ERRORS_KEY,
                    validationException.getErrors().entrySet().stream()
                            .flatMap(error -> {
                                var field = error.getKey();
                                return error.getValue().stream()
                                        .map(detail -> new ProblemFieldDto(field, detail));
                            }));
            addExceptionInfoToProblemDetails(problem, validationException);
        } else if (exception instanceof DomainException domainException) {
            addExceptionInfoToProblemDetails(problem, domainException);
            statusCode = getStatusCodeByExceptionType(domainException.getClass());
        } else {
            problem.setTitle("Internal server error.");

            var profile = Arrays.stream(environment.getActiveProfiles()).findFirst();
            if (profile.isEmpty() || !profile.get().equals("prod")) {
                problem.setProperty(
                        "debug_exception",
                        Map.ofEntries(
                                entry("Type", exception.getClass().getSimpleName()),
                                entry("Message", exception.getMessage() == null ? "" : exception.getMessage()),
                                entry("StackTrace", ExceptionUtils.getStackTrace(exception))));
            }

            statusCode = getStatusCodeByExceptionType(exception.getClass());
            logger.error(exception.getMessage(), exception);
        }

        problem.setStatus(statusCode);
        return problem;
    }
}