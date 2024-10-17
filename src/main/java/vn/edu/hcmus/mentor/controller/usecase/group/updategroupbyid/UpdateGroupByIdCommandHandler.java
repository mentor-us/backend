package vn.edu.hcmus.mentor.controller.usecase.group.updategroupbyid;

import an.awesome.pipelinr.Command;
import vn.edu.hcmus.mentor.controller.exception.DomainException;
import vn.edu.hcmus.mentor.controller.exception.ValidationException;
import vn.edu.hcmus.mentor.controller.payload.ReturnCodeConstants;
import vn.edu.hcmus.mentor.controller.usecase.group.common.GroupDetailDto;
import vn.edu.hcmus.mentor.domain.AuditRecord;
import vn.edu.hcmus.mentor.domain.constant.ActionType;
import vn.edu.hcmus.mentor.domain.constant.DomainType;
import vn.edu.hcmus.mentor.domain.constant.GroupStatus;
import vn.edu.hcmus.mentor.domainservice.GroupDomainService;
import vn.edu.hcmus.mentor.repository.GroupCategoryRepository;
import vn.edu.hcmus.mentor.repository.GroupRepository;
import vn.edu.hcmus.mentor.repository.UserRepository;
import vn.edu.hcmus.mentor.security.principal.LoggedUserAccessor;
import vn.edu.hcmus.mentor.service.AuditRecordService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handler for {@link UpdateGroupByIdCommandHandler}.
 */
@Component
@RequiredArgsConstructor
public class UpdateGroupByIdCommandHandler implements Command.Handler<UpdateGroupByIdCommand, GroupDetailDto> {

    private final Logger logger = LoggerFactory.getLogger(UpdateGroupByIdCommandHandler.class);
    private final LoggedUserAccessor loggedUserAccessor;
    private final ModelMapper modelMapper;
    private final GroupRepository groupRepository;
    private final GroupCategoryRepository groupCategoryRepository;
    private final GroupDomainService groupDomainService;
    private final AuditRecordService auditRecordService;
    private final UserRepository userRepository;

    @Override
    public GroupDetailDto handle(UpdateGroupByIdCommand command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var group = groupRepository.findById(command.getId()).orElseThrow(() -> new DomainException("Không tìm thấy nhóm với id " + command.getId(), ReturnCodeConstants.GROUP_NOT_FOUND));

        if (groupRepository.existsByName(command.getName()) && !command.getName().equals(group.getName())) {
            throw new DomainException("Tên nhóm đã tồn tại", ReturnCodeConstants.GROUP_DUPLICATE_GROUP);
        }

        var groupCategory = groupCategoryRepository.findById(command.getGroupCategory()).orElseThrow(() -> new DomainException("Không tìm thấy loại nhóm với id " + command.getGroupCategory(), ReturnCodeConstants.GROUP_NOT_FOUND));

        if (!groupDomainService.isStartAndEndTimeValid(command.getTimeStart(), command.getTimeEnd())) {
            throw new DomainException("Thời gian không hợp lệ", ReturnCodeConstants.GROUP_TIME_START_TOO_FAR_FROM_NOW);
        }

        if (command.getStatus() == null) {
            throw new ValidationException("Trạng thái không hợp lệ", ReturnCodeConstants.GROUP_NOT_ENOUGH_FIELDS);
        }

        var detailUpdate = new StringBuilder();
        if (!command.getName().equals(group.getName())) {
            group.setName(command.getName());
            detailUpdate.append("\n").append("Tên nhóm: ").append(command.getName());
        }
        if (!command.getDescription().equals(group.getDescription())) {
            group.setDescription(command.getDescription());
            detailUpdate.append("\n").append("Mô tả: ").append(command.getDescription());
        }
        if (!command.getTimeStart().equals(group.getTimeStart())) {
            group.setTimeStart(command.getTimeStart());
            detailUpdate.append("\n").append("Thời gian bắt đầu: ").append(command.getTimeStart());
        }
        if (!command.getTimeEnd().equals(group.getTimeEnd())) {
            group.setTimeEnd(command.getTimeEnd());
            detailUpdate.append("\n").append("Thời gian kết thúc: ").append(command.getTimeEnd());
        }
        if (!command.getStatus().equals(GroupStatus.DISABLED)) {
            var newGroupStatus = groupDomainService.getGroupStatus(command.getTimeStart(), command.getTimeEnd());
            if (newGroupStatus.equals(group.getStatus())) {
                group.setStatus(newGroupStatus);
                detailUpdate.append("\n").append("Trạng thái: ").append(newGroupStatus);
            }
            if (!groupCategory.equals(group.getGroupCategory())) {
                group.setGroupCategory(groupCategory);
                detailUpdate.append("\n").append("Loại nhóm: ").append(groupCategory.getName());
            }
        } else {
            group.setStatus(GroupStatus.DISABLED);
            detailUpdate.append("\n").append("Trạng thái: ").append(GroupStatus.DISABLED);
        }

        group = groupRepository.save(group);

        if (!detailUpdate.isEmpty()) {
            auditRecordService.save(AuditRecord.builder()
                    .user(userRepository.findById(currentUserId).orElse(null))
                    .action(ActionType.UPDATED)
                    .domain(DomainType.GROUP)
                    .entityId(group.getId())
                    .detail(String.format("Cập nhật nhóm %s: %s", group.getName(), detailUpdate))
                    .build());
        }

        logger.info("User id {} updated group {}", currentUserId, group.getId());

        return modelMapper.map(group, GroupDetailDto.class);
    }
}