package com.hcmus.mentor.backend.security.middlewares;

import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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
public class ApiExceptionMiddleware {

    private static final String errorsKey = "errors";
    private static final String codeKey = "code";
    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionMiddleware.class);
    private static final Map<Class, HttpStatus> exceptionStatusCodes =
            Map.ofEntries(entry(DomainException.class, HttpStatus.BAD_REQUEST));
    private final Environment environment;

    /**
     * Global exception handler.
     *
     * @param ex exception.
     * @return The {@link ProblemDetail}.
     */
    @ExceptionHandler({DomainException.class, ValidationException.class})
    public ProblemDetail handlerDomainException(Exception ex) {
        return getObjectByException(ex);
    }

    private static void addExceptionInfoToProblemDetails(
            ProblemDetail problemDetail, DomainException exception) {
        problemDetail.setTitle(exception.getMessage());
        problemDetail.setType(URI.create(exception.getClass().getSimpleName()));
        addCodeToProblemDetails(problemDetail, exception.getCode());
    }

    private static void addCodeToProblemDetails(ProblemDetail problemDetail, String code) {
        if (code != null) {
            problemDetail.setProperty(codeKey, code);
        }
    }

    private static HttpStatus getStatusCodeByExceptionType(Class exceptionType) {
        if (exceptionType == DomainException.class) {
            return HttpStatus.BAD_REQUEST;
        }
        for (Map.Entry<Class, HttpStatus> entry : exceptionStatusCodes.entrySet()) {
            if (entry.getKey().isAssignableFrom(exceptionType)) {
                return entry.getValue();
            }
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private ProblemDetail getObjectByException(Exception exception) {
        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        HttpStatus statusCode = HttpStatus.BAD_REQUEST;

        if (exception instanceof ValidationException validationException) {
            problem.setProperty(
                    errorsKey,
                    validationException.getErrors().entrySet().stream()
                            .flatMap(
                                    error -> {
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
                                entry("Message", exception.getMessage()),
                                entry("StackTrace", ExceptionUtils.getStackTrace(exception))));
            }

            statusCode = getStatusCodeByExceptionType(exception.getClass());
            logger.error(exception.getMessage(), exception);
        }

        problem.setStatus(statusCode);
        return problem;
    }
}
