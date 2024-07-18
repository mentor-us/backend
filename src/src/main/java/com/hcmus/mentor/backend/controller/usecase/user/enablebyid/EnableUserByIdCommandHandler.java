package com.hcmus.mentor.backend.controller.usecase.user.enablebyid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.domain.AuditRecord;
import com.hcmus.mentor.backend.domain.constant.ActionType;
import com.hcmus.mentor.backend.domain.constant.DomainType;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.AuditRecordService;
import com.hcmus.mentor.backend.service.UserService;
import com.hcmus.mentor.backend.service.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnableUserByIdCommandHandler implements Command.Handler<EnableUserByIdCommand, UserDto> {

    private final Logger logger = LoggerFactory.getLogger(EnableUserByIdCommandHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final UserService userService;
    private final AuditRecordService auditRecordService;

    @Override
    public UserDto handle(EnableUserByIdCommand command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();
        var currentUser = userService.findById(currentUserId).orElseThrow(() -> new DomainException("Không tìm thấy người dùng hiện tại"));

        var user = userService.findById(command.getId()).orElseThrow(() -> new DomainException(String.format("Không tìm thấy người dùng với ID %s", command.getId())));

        if (!user.isStatus()) {
            user.setStatus(false);

            userService.save(user);

            var auditRecord = AuditRecord.builder()
                    .entityId(user.getId())
                    .user(currentUser)
                    .action(ActionType.UPDATED)
                    .domain(DomainType.USER)
                    .detail(String.format("Người dùng %s đã được kích hoạt", user.getEmail()))
                    .user(currentUser)
                    .build();

            auditRecordService.save(auditRecord);

            logger.info("{}: User is enable with ID {}, Email {}", this.getClass().getSimpleName(), user.getId(), user.getEmail());
        }

        return modelMapper.map(user, UserDto.class);
    }
}
