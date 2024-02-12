package com.hcmus.mentor.backend.security.handler;

/**
 * Problem field DTO.
 *
 * @param field  Field name.
 * @param detail Field error detail.
 */
public record ProblemFieldDto(String field, String detail) {
}
