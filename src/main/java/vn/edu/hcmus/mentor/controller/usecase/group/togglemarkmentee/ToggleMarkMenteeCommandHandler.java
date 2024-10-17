package vn.edu.hcmus.mentor.controller.usecase.group.togglemarkmentee;

import an.awesome.pipelinr.Command;
import vn.edu.hcmus.mentor.controller.exception.DomainException;
import vn.edu.hcmus.mentor.controller.exception.ForbiddenException;
import vn.edu.hcmus.mentor.domain.AuditRecord;
import vn.edu.hcmus.mentor.domain.User;
import vn.edu.hcmus.mentor.domain.constant.ActionType;
import vn.edu.hcmus.mentor.domain.constant.DomainType;
import vn.edu.hcmus.mentor.repository.GroupRepository;
import vn.edu.hcmus.mentor.repository.GroupUserRepository;
import vn.edu.hcmus.mentor.repository.UserRepository;
import vn.edu.hcmus.mentor.security.principal.LoggedUserAccessor;
import vn.edu.hcmus.mentor.service.AuditRecordService;
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