package com.hcmus.mentor.backend.controller.usecase.user.addaddtionalemail;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.service.UserService;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;

/**
 * Command to add additional email to user account.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddAdditionalEmailCommand implements Command<UserService.UserReturnService> {

    /**
     * ID of user account.
     */
    @Required
    private String userId;

    /**
     * Email of user account.
     */
    @Required
    @Email(message = "It not email")
    private String additionalEmail;
}
