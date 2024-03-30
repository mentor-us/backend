package com.hcmus.mentor.backend.controller.exception;

/**
 * The Exception occurs in the domain part of the application if an entity is not found by the key.
 * It can be mapped to 404 HTTP status code.
 */
public class NotFoundException extends DomainException {

    /**
     * Constructor.
     */
    public NotFoundException() {
        super("Not found");
    }

    /**
     * Constructor.
     *
     * @param code Optional description code for this exception
     */
    public NotFoundException(int code) {
        super("Not found", 404);
    }

    /**
     * Constructor.
     *
     * @param message The message that describes the error.
     */
    public NotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param message The message that describes the error.
     * @param code    Optional description code for this exception.
     */
    public NotFoundException(String message, int code) {
        super(message, code);
    }

    /**
     * Constructor.
     *
     * @param message The message that describes the error.
     * @param code    Optional description code for this exception.
     */
    public NotFoundException(String message, String code) {
        super(message, code);
    }

    /**
     * Constructor.
     *
     * @param message        The message that describes the error
     * @param innerException The exception that is the cause of the current exception
     */
    public NotFoundException(String message, Exception innerException) {
        super(message, innerException);
    }

    /**
     * Constructor.
     *
     * @param message        The message that describes the error.
     * @param innerException The exception that is the cause of the current exception.
     * @param code           Optional description code for this exception.
     */
    public NotFoundException(String message, Exception innerException, int code) {
        super(message, innerException, code);
    }

    /**
     * Constructor.
     *
     * @param message        The message that describes the error.
     * @param innerException The exception that is the cause of the current exception.
     * @param code           Optional description code for this exception.
     */
    public NotFoundException(String message, Exception innerException, String code) {
        super(message, innerException, code);
    }
}
