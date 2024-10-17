package vn.edu.hcmus.mentor.controller.usecase.group.enabledisablestatusgroupbyid;

import an.awesome.pipelinr.Command;
import vn.edu.hcmus.mentor.controller.exception.DomainException;
import vn.edu.hcmus.mentor.controller.exception.ForbiddenException;
import vn.edu.hcmus.mentor.controller.usecase.group.common.GroupDetailDto;
import vn.edu.hcmus.mentor.domain.AuditRecord;
import vn.edu.hcmus.mentor.domain.constant.ActionType;
import vn.edu.hcmus.mentor.domain.constant.DomainType;
import vn.edu.hcmus.mentor.domain.constant.GroupStatus;
import vn.edu.hcmus.mentor.domainservice.GroupDomainService;
import vn.edu.hcmus.mentor.repository.GroupRepository;
import vn.edu.hcmus.mentor.repository.UserRepository;
import vn.edu.hcmus.mentor.security.principal.LoggedUserAccessor;
import vn.edu.hcmus.mentor.service.AuditRecordService;
import vn.edu.hcmus.mentor.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnableDisableGroupByIdCommandHandler implements Command.Handler<EnableDisableGroupByIdCommand, GroupDetailDto> {

    private final Logger logger = LoggerFactory.getLogger(EnableDisableGroupByIdCommandHandler.class);
    private final LoggedUserAccessor loggedUserAccessor;
    private final ModelMapper modelMapper;
    private final PermissionService permissionService;
    private final GroupRepository groupRepository;
    private final GroupDomainService groupDomainService;
    private final AuditRecordService auditRecordService;
    private final UserRepository userRepository;

    @Override
    public GroupDetailDto handle(EnableDisableGroupByIdCommand command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        if (!permissionService.isAdmin(currentUserId, 0)) {
            throw new ForbiddenException("Không có quyền chỉnh sửa nhóm này");
        }

        var group = groupRepository.findById(command.getId()).orElseThrow(() -> new DomainException("Không tìm thấy nhóm với id " + command.getId()));
        var groupStatus = group.getStatus();
        var isUpdate = false;

        if (command.getStatus().equals(GroupStatus.DISABLED) && !groupStatus.equals(GroupStatus.DISABLED)) {
            group.setStatus(GroupStatus.DISABLED);
            isUpdate = true;

        } else {
            // Activate group.
            var status = groupDomainService.getGroupStatus(group.getTimeStart(), group.getTimeEnd());

            if (!group.getStatus().equals(status)) {
                group.setStatus(status);
                isUpdate = true;

            }
        }

        if (isUpdate) {
            groupRepository.save(group);
            auditRecordService.save(AuditRecord.builder()
                    .user(userRepository.findById(currentUserId).orElse(null))
                    .action(ActionType.UPDATED)
                    .domain(DomainType.GROUP)
                    .entityId(group.getId())
                    .detail(String.format("%s nhóm %s", group.getStatus().equals(GroupStatus.DISABLED) ? "Đã vô hiệu hóa" : "Đã kích hoạt", group.getName()))
                    .build());

            logger.info("Đã cập nhật trạng thái nhóm với id {} thành {}", command.getId(), command.getStatus());
        }

        return modelMapper.map(group, GroupDetailDto.class);
    }
}