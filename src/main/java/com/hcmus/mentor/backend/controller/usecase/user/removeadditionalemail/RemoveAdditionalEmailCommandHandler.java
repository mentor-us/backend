package com.hcmus.mentor.backend.controller.usecase.user.removeadditionalemail;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.domain.AuditRecord;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.ActionType;
import com.hcmus.mentor.backend.domain.constant.DomainType;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.service.AuditRecordService;
import com.hcmus.mentor.backend.service.dto.UserServiceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.hcmus.mentor.backend.controller.payload.ReturnCodeConstants.SUCCESS;
import static com.hcmus.mentor.backend.controller.payload.ReturnCodeConstants.USER_NOT_FOUND;

/**
 * Handler for {@link RemoveAdditionalEmailCommand}.
 */
@Component
@RequiredArgsConstructor
public class RemoveAdditionalEmailCommandHandler implements Command.Handler<RemoveAdditionalEmailCommand, UserServiceDto> {

    private final UserRepository userRepository;
    private final AuditRecordService auditRecordService;

    /**
     * @param command Command to remove additional email from user account.
     * @return UserReturnService
     */
    @Override
    public UserServiceDto handle(RemoveAdditionalEmailCommand command) {
        if (userRepository.findByEmail(command.getAdditionalEmail()).isPresent())
            return new UserServiceDto(USER_NOT_FOUND, "Can\'t not remove primary email!", null);

        Optional<User> userOptional = userRepository.findById(command.getUserId());
        if (userOptional.isEmpty()) {
            return new UserServiceDto(USER_NOT_FOUND, "Not found user", null);
        }

        var user = userOptional.get();
        var additionEmails = user.getAdditionalEmails();
        if (additionEmails.isEmpty() || !additionEmails.contains(command.getAdditionalEmail())) {
            return new UserServiceDto(USER_NOT_FOUND, "Not found additional email!", null);
        }
        additionEmails.remove(command.getAdditionalEmail());
        user.setAdditionalEmails(additionEmails);
        userRepository.save(user);

        auditRecordService.save(AuditRecord.builder()
                .entityId(user.getId())
                .user(user)
                .action(ActionType.UPDATED)
                .domain(DomainType.USER)
                .detail(String.format("Người dung %s đã xóa email phụ %s", user.getEmail(), command.getAdditionalEmail()))
                .build());

        return new UserServiceDto(SUCCESS, "Remove addition email success.", user);

    }
}