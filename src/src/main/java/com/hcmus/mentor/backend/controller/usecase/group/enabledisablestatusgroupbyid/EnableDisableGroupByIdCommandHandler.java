package com.hcmus.mentor.backend.controller.usecase.group.enabledisablestatusgroupbyid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
import com.hcmus.mentor.backend.controller.usecase.group.common.GroupDetailDto;
import com.hcmus.mentor.backend.domain.AuditRecord;
import com.hcmus.mentor.backend.domain.constant.ActionType;
import com.hcmus.mentor.backend.domain.constant.DomainType;
import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import com.hcmus.mentor.backend.domainservice.GroupDomainService;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.AuditRecordService;
import com.hcmus.mentor.backend.service.PermissionService;
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