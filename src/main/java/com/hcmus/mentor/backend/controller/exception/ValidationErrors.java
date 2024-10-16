package com.hcmus.mentor.backend.controller.exception;

import lombok.Getter;
import org.springframework.validation.BindingResult;

import java.util.*;

/**
 * The collection of key-value validation messages. Allows accumulating errors per key.
 */
@Getter
public class ValidationErrors extends HashMap<String, List<String>> {

    /**
     * Default summary validation key. Should contain the overall message.
     */
    private final String summaryKey = "";
    /**
     * Returns {@code true} if there were any errors added.
     */
    public Boolean hasError = !this.isEmpty();

    /**
     * Constructor.
     */
    public ValidationErrors() {
    }

    /**
     * Constructor.
     *
     * @param error Initial errors to initialize withInitial errors to initialize with.
     */
    public ValidationErrors(Map<String, List<String>> error) {
        super(error);
    }

    /**
     * Constructor.
     *
     * @param errors {@link BindingResult} return from validate model.
     */
    public ValidationErrors(BindingResult errors) {
        for (var error : errors.getFieldErrors()) {
            if (this.containsKey(error.getField())) {
                List<String> messages = this.get(error.getField());
                messages.add(error.getDefaultMessage());
            } else {
                this.put(
                        error.getField(),
                        new ArrayList<>(Collections.singletonList(error.getDefaultMessage())));
            }
        }
    }
}
