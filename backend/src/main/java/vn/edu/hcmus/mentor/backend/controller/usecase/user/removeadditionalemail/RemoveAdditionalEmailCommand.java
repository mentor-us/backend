package vn.edu.hcmus.mentor.backend.controller.usecase.user.removeadditionalemail;

import an.awesome.pipelinr.Command;
import vn.edu.hcmus.mentor.backend.service.dto.UserServiceDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Command to remove additional email from user account.
 */
@Data
@Builder
@AllArgsConstructor
public class RemoveAdditionalEmailCommand implements Command<UserServiceDto> {
    /**
     * ID of user account.
     */
    private String userId;

    /**
     * Email of user account.
     */
    private String additionalEmail;
}
