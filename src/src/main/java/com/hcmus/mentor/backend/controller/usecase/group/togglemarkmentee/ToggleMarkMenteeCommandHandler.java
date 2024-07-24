package com.hcmus.mentor.backend.controller.usecase.group.togglemarkmentee;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
import com.hcmus.mentor.backend.domain.AuditRecord;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.ActionType;
import com.hcmus.mentor.backend.domain.constant.DomainType;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.GroupUserRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.AuditRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Handler for {@link ToggleMarkMenteeCommand}.
 */
@Component
@RequiredArgsConstructor
public class ToggleMarkMenteeCommandHandler implements Command.Handler<ToggleMarkMenteeCommand, Void> {

    private final GroupRepository groupRepository;
    private final GroupUserRepository groupUserRepository;
    private final AuditRecordService auditRecordService;
    private final UserRepository userRepository;
    private final LoggedUserAccessor loggedUserAccessor;

    /**
     * @param command command to toggle mark mentee.
     * @return null.
     */
    @Override
    public Void handle(ToggleMarkMenteeCommand command) {
        var group = groupRepository.findById(command.getGroupId())
                .orElseThrow(() -> new DomainException("Không tìm thấy nhóm với id " + command.getGroupId()));

        if (group.getGroupUsers().stream().noneMatch(gu -> Objects.equals(gu.getUser().getId(), command.getCurrentUserId()))) {
            throw new ForbiddenException("Không có quyền thực hiện hành động này. Bạn không phải là thành viên của nhóm.");
        }
        if (!group.isMentor(command.getCurrentUserId())) {
            throw new ForbiddenException("Không có quyền thực hiện hành động này. Bạn không phải là mentor của nhóm.");
        }

        var groupUser = group.getGroupUsers().stream()
                .filter(gu -> Objects.equals(gu.getUser().getId(), command.getMenteeId()))
                .findFirst()
                .orElseThrow(() -> new DomainException("Không phải là thành viên của nhóm."));

        if (command.isMarked() == groupUser.isMarked()) {
            if (command.isMarked()) {
                throw new DomainException("Mentee đã được đánh dấu rồi.");
            } else {
                throw new DomainException("Mentee chưa được đánh dấu.");
            }
        }
        groupUser.setMarked(command.isMarked());
        groupUserRepository.save(groupUser);

        auditRecordService.save(AuditRecord.builder()
                .action(ActionType.UPDATED)
                .domain(DomainType.GROUP)
                .user(userRepository.findById(loggedUserAccessor.getCurrentUserId()).orElse(null))
                .detail(String.format("%s mentee %s trong nhóm %s", command.isMarked() ? "Đã đánh dấu" : "Đã bỏ đánh dấu", userRepository.findById(command.getMenteeId()).map(User::getEmail).orElse(null), group.getName()))
                .entityId(group.getId())
                .build());

        return null;
    }
}