package com.hcmus.mentor.backend.controller.payload.request;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddAdditionEmailRequest {

    @Required
    @Email(message = "It not email")
    private String additionalEmail;

}
