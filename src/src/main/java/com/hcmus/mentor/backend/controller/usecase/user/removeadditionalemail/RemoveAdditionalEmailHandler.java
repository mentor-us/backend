package com.hcmus.mentor.backend.controller.usecase.user.removeadditionalemail;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.service.dto.UserServiceDto;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.hcmus.mentor.backend.controller.payload.returnCode.SuccessCode.SUCCESS;
import static com.hcmus.mentor.backend.controller.payload.returnCode.UserReturnCode.NOT_FOUND;

/**
 * Handler for {@link RemoveAdditionalEmailCommand}.
 */
@Component
public class RemoveAdditionalEmailHandler implements Command.Handler<RemoveAdditionalEmailCommand, UserServiceDto> {

    private final UserRepository userRepository;

    public RemoveAdditionalEmailHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * @param command Command to remove additional email from user account.
     * @return UserReturnService
     */
    @Override
    public UserServiceDto handle(RemoveAdditionalEmailCommand command) {
        if (userRepository.findByEmail(command.getAdditionalEmail()).isPresent())
            return new UserServiceDto(NOT_FOUND, "Can't not remove primary email!", null);

        Optional<User> userOptional = userRepository.findById(command.getUserId());
        if (userOptional.isEmpty()) {
            return new UserServiceDto(NOT_FOUND, "Not found user", null);
        }

        var user = userOptional.get();
        var additionEmails = user.getAdditionalEmails();
        if (additionEmails.isEmpty() || !additionEmails.contains(command.getAdditionalEmail())) {
            return new UserServiceDto(NOT_FOUND, "Not found additional email!", null);
        }
        additionEmails.remove(command.getAdditionalEmail());
        user.setAdditionalEmails(additionEmails);
        userRepository.save(user);

        return new UserServiceDto(SUCCESS, "Remove addition email success.", user);

    }
}
