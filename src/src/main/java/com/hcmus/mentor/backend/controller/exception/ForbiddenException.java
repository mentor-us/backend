package com.hcmus.mentor.backend.controller.exception;

import lombok.Getter;

@Getter
public class ForbiddenException extends DomainException {

    /**
     * Constructor.
     */
    public ForbiddenException() {
        super("Forbidden");
    }


    /**
     * @param code Optional description code for this exception
     */
    public ForbiddenException(int code) {
        super("Forbidden", 403);
    }

    /**
     * @param message The message that describes the error.
     */
    public ForbiddenException(String message){
        super(message);
    }

    /**
     * @param message The message that describes the error.
     * @param code Optional description code for this exception.
     */
    public ForbiddenException(String message, int code){
        super(message, code);
    }

    /**
     * @param message The message that describes the error.
     * @param code Optional description code for this exception.
     */
    public ForbiddenException(String message, String code){
        super(message, code);
    }

    /**
     * @param message The message that describes the error
     * @param innerException The exception that is the cause of the current exception
     */
    public ForbiddenException(String message, Exception innerException) {
        super(message, innerException);
    }

    /**
     * @param message The message that describes the error.
     * @param innerException The exception that is the cause of the current exception.
     * @param code Optional description code for this exception.
     */
    public ForbiddenException(String message, Exception innerException, int code) {
        super(message, innerException, code);
    }

    /**
     * @param message The message that describes the error.
     * @param innerException The exception that is the cause of the current exception.
     * @param code Optional description code for this exception.
     */
    public ForbiddenException(String message, Exception innerException, String code){
        super(message, innerException, code);
    }

}
