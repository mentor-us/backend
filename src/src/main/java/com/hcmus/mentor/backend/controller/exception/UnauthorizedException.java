package com.hcmus.mentor.backend.controller.exception;

import lombok.Getter;

/**
 * A domain user is not unauthorized exception. It can be mapped to 401 HTTP status code.
 */
@Getter
public class UnauthorizedException extends DomainException {

    /**
     * Constructor.
     */
    public UnauthorizedException() {
        super("Unauthorized");
    }

    /**
     * Constructor.
     *
     * @param code Optional description code for this exception.
     */
    public UnauthorizedException(int code) {
        super("Unauthorized", 401);
    }

    /**
     * Constructor.
     *
     * @param message The message that describes the error.
     */
    public UnauthorizedException(String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param message The message that describes the error.
     * @param code    Optional description code for this exception.
     */
    public UnauthorizedException(String message, int code) {
        super(message, code);
    }

    /**
     * Constructor.
     *
     * @param message The message that describes the error.
     * @param code    Optional description code for this exception.
     */
    public UnauthorizedException(String message, String code) {
        super(message, code);
    }

    /**
     * Constructor.
     *
     * @param message        The message that describes the error.
     * @param innerException The exception that is the cause of the current exception.
     */
    public UnauthorizedException(String message, Exception innerException) {
        super(message, innerException);
    }

    /**
     * Constructor.
     *
     * @param message        The message that describes the error.
     * @param innerException The exception that is the cause of the current exception.
     * @param code           Optional description code for this exception.
     */
    public UnauthorizedException(String message, Exception innerException, int code) {
        super(message, innerException, code);
    }

    /**
     * Constructor.
     *
     * @param message        The message that describes the error.
     * @param innerException The exception that is the cause of the current exception.
     * @param code           Optional description code for this exception.
     */
    public UnauthorizedException(String message, Exception innerException, String code) {
        super(message, innerException, code);
    }
}
