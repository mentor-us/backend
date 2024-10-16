package com.hcmus.mentor.backend.controller.payload.request.users;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;

/**
 * Remove additional email request payload
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RemoveAdditionalEmailRequest {

    @Email
    @Required
    private String additionalEmail;
}