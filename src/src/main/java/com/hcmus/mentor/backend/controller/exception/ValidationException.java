package com.hcmus.mentor.backend.controller.exception;

import lombok.Getter;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Map;

/**
 * The validation exception. It can be mapped to 400 HTTP status codeThe validation exception.
 */
@Getter
public class ValidationException extends DomainException {

    /**
     * Errors dictionary. Key is a member name, value is enumerable of error messages. Empty member
     * name relates to a summary error message.
     */
    private ValidationErrors errors = new ValidationErrors();

    /**
     * Constructor.
     */
    public ValidationException() {
        super("ValidationErrors");
    }

    /**
     * Constructor.
     *
     * @param code Optional description code for this exception.
     */
    public ValidationException(int code) {
        super("ValidationErrors", code);
    }

    /**
     * Constructor.
     *
     * @param message The message that describes the error.
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param message The message that describes the error.
     * @param code    Optional description code for this exception.
     */
    public ValidationException(String message, int code) {
        super(message, code);
    }

    /**
     * Constructor.
     *
     * @param message The message that describes the error.
     * @param code    Optional description code for this exception.
     */
    public ValidationException(String message, String code) {
        super(message, code);
    }

    /**
     * Constructor.
     *
     * @param message        The message that describes the error.
     * @param innerException Optional description code for this exception.
     */
    public ValidationException(String message, Exception innerException) {
        super(message, innerException);
    }

    /**
     * Constructor.
     *
     * @param message        The message that describes the error.
     * @param innerException The exception that is the cause of the current exception.
     * @param code           Optional description code for this exception.
     */
    public ValidationException(String message, Exception innerException, int code) {
        super(message, innerException, code);
    }

    /**
     * Constructor.
     *
     * @param message        The message that describes the error.
     * @param innerException The exception that is the cause of the current exception.
     * @param code           Optional description code for this exception.
     */
    public ValidationException(String message, Exception innerException, String code) {
        super(message, innerException, code);
    }

    /**
     * Constructor.
     *
     * @param error Member error dictionary.
     */
    public ValidationException(Map<String, List<String>> error) {
        super("ValidationErrors");

        this.errors = new ValidationErrors(error);
    }

    /**
     * Constructor.
     *
     * @param errors {@link BindingResult} return from validate model.
     */
    public ValidationException(BindingResult errors) {
        super("Please refer to the errors for additional details.");

        this.errors = new ValidationErrors(errors);
    }
}
