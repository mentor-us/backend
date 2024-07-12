package com.hcmus.mentor.backend.controller.usecase.user.addaddtionalemail;


import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.service.dto.UserServiceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.hcmus.mentor.backend.controller.payload.ReturnCodeConstants.SUCCESS;
import static com.hcmus.mentor.backend.controller.payload.ReturnCodeConstants.USER_DUPLICATE_EMAIL;
import static com.hcmus.mentor.backend.controller.payload.ReturnCodeConstants.USER_NOT_FOUND;

/**
 * Handler for {@link AddAdditionalEmailCommand}.
 */
@Component
@RequiredArgsConstructor
public class AddAdditionalEmailCommandHandler implements Command.Handler<AddAdditionalEmailCommand, UserServiceDto> {

    private final UserRepository userRepository;

    /**
     * @param command command to add additional email to user account.
     * @return result of adding additional email to user account.
     */
    @Override
    public UserServiceDto handle(AddAdditionalEmailCommand command) {
        if (userRepository.findByAdditionalEmailsContains(command.getAdditionalEmail()).isPresent() || userRepository.findByEmail(command.getAdditionalEmail()).isPresent()) {
            return new UserServiceDto(USER_DUPLICATE_EMAIL, "Duplicate email", null);
        }

        Optional<User> userOptional = userRepository.findById(command.getUserId());
        if (userOptional.isEmpty()) {
            return new UserServiceDto(USER_NOT_FOUND, "Not found user", null);
        }

        var user = userOptional.get();
        var additionEmails = user.getAdditionalEmails();
        additionEmails.add(command.getAdditionalEmail());
        user.setAdditionalEmails(additionEmails);
        userRepository.save(user);

        return new UserServiceDto(SUCCESS, "Add addition email success", user);
    }

}
