package com.hcmus.mentor.backend.controller.usecase.user.addaddtionalemail;


import an.awesome.pipelinr.Command;
import static com.hcmus.mentor.backend.controller.payload.returnCode.SuccessCode.SUCCESS;
import static com.hcmus.mentor.backend.controller.payload.returnCode.UserReturnCode.DUPLICATE_EMAIL;
import static com.hcmus.mentor.backend.controller.payload.returnCode.UserReturnCode.NOT_FOUND;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.service.UserService;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Handler for {@link AddAdditionalEmailCommand}.
 */
@Component
public class AddAdditionalEmailCommandHandler implements Command.Handler<AddAdditionalEmailCommand, UserService.UserReturnService> {

    private final UserRepository userRepository;

    /**
     * @param userRepository repository of user.
     */
    public AddAdditionalEmailCommandHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * @param command command to add additional email to user account.
     * @return result of adding additional email to user account.
     */
    @Override
    public UserService.UserReturnService handle(AddAdditionalEmailCommand command) {
        if (userRepository.findByAdditionalEmailsContains(command.getAdditionalEmail()).isPresent() || userRepository.findByEmail(command.getAdditionalEmail()).isPresent()) {
            return new UserService.UserReturnService(DUPLICATE_EMAIL, "Duplicate email", null);
        }

        Optional<User> userOptional = userRepository.findById(command.getUserId());
        if (userOptional.isEmpty()) {
            return new UserService.UserReturnService(NOT_FOUND, "Not found user", null);
        }

        var user = userOptional.get();
        var additionEmails = user.getAdditionalEmails();
        additionEmails.add(command.getAdditionalEmail());
        user.setAdditionalEmails(additionEmails);
        userRepository.save(user);

        return new UserService.UserReturnService(SUCCESS, "Add addition email success", user);
    }

}
