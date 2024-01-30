package com.hcmus.mentor.backend.security.middlewares;

import lombok.Getter;

/**
 * Problem field DTO.
 */
@Getter
public class ProblemFieldDto {

    /**
     * Field name.
     */
    private final String field;

    /**
     * Field error detail.
     */
    private final String detail;

    /**
     * Constructor.
     *
     * @param field  Field name.
     * @param detail Field error detail.
     */
    public ProblemFieldDto(String field, String detail) {
        this.field = field;
        this.detail = detail;
    }
}
