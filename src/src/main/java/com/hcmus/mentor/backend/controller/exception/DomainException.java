package com.hcmus.mentor.backend.controller.exception;

import lombok.Getter;

/**
 * The exception occurs in domain part of the application. It can be business logic or validation
 * exception. The message can be used as display messages to the end-user. InnerException should
 * contain actual system exception.
 */
@Getter
public class DomainException extends RuntimeException {

    /**
     * Optional description code for this exception.
     */
    private String code = "";

    /**
     * Constructor.
     */
    public DomainException() {
        super("Error");
    }

    /**
     * Constructor.
     *
     * @param code Optional description code for this exception.
     */
    public DomainException(int code) {
        super("Error");

        this.code = String.valueOf(code);
    }

    /**
     * Constructor.
     *
     * @param message The message that describes the error.
     */
    public DomainException(String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param message The message that describes the errorThe message that describes the error.
     * @param code    Optional description code for this exception
     */
    public DomainException(String message, int code) {
        super(message);

        this.code = String.valueOf(code);
    }

    /**
     * Constructor.
     *
     * @param message The message that describes the errorThe message that describes the error.
     * @param code    Optional description code for this exception.
     */
    public DomainException(String message, String code) {
        super(message);

        this.code = code;
    }

    /**
     * Constructor.
     *
     * @param message        The message that describes the errorThe message that describes the error.
     * @param innerException The exception that is the cause of the current exception.
     */
    public DomainException(String message, Exception innerException) {
        super(message, innerException);
    }

    /**
     * Constructor.
     *
     * @param message        The message that describes the errorThe message that describes the error.
     * @param innerException The exception that is the cause of the current exception.
     * @param code           Optional description code for this exception.
     */
    public DomainException(String message, Exception innerException, int code) {
        super(message, innerException);

        this.code = String.valueOf(code);
    }

    /**
     * Constructor.
     *
     * @param message        The message that describes the error.
     * @param innerException The exception that is the cause of the current exception.
     * @param code           Optional description code for this exception.
     */
    public DomainException(String message, Exception innerException, String code) {
        super(message, innerException);

        this.code = code;
    }
}
