package com.hcmus.mentor.backend.controller.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import org.springframework.validation.BindingResult;

/**
 * The collection of key-value validation messages. Allows accumulating errors per key.
 */
@Getter
public class ValidationErrors extends HashMap<String, List<String>> {

    /**
     * Returns {@code true} if there were any errors added.
     */
    public Boolean hasError = !this.isEmpty();
    /**
     * Default summary validation key. Should contain the overall message.
     */
    private String summaryKey = "";

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
