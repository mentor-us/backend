package com.hcmus.mentor.backend.controller.usecase.group.promotedemoteuser;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
import com.hcmus.mentor.backend.controller.usecase.group.common.GroupDetailDto;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.GroupUserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromoteDenoteUserByIdCommandHandler implements Command.Handler<PromoteDenoteUserByIdCommand, GroupDetailDto> {

    private final Logger logger = LoggerFactory.getLogger(PromoteDenoteUserByIdCommandHandler.class);
    private final LoggedUserAccessor loggedUserAccessor;
    private final ModelMapper modelMapper;
    private final PermissionService permissionService;
    private final GroupRepository groupRepository;
    private final GroupUserRepository groupUserRepository;

    @Override
    public GroupDetailDto handle(PromoteDenoteUserByIdCommand command) {
        var curerntUserId = loggedUserAccessor.getCurrentUserId();

        if (!permissionService.isAdmin(curerntUserId, 0)) {
            throw new ForbiddenException("Không có quyền chuyển vai trò thành viên trong nhóm");
        }

        var group = groupRepository.findById(command.getGroupId()).orElseThrow(() -> new DomainException("Không tìm thấy nhóm"));
        var groupUser = groupUserRepository.findByUserIdAndGroupId(command.getUserId(), group.getId()).orElseThrow(() -> new DomainException("Người dùng không thuộc nhóm"));

        if (command.getToMentor()) {
            if (groupUser.isMentor()) {
                throw new DomainException("Người dùng đã là mentor");
            } else {
                groupUser.setMentor(true);
                groupUserRepository.save(groupUser);

                logger.info("User {} promoted to mentor in group {}", command.getUserId(), command.getGroupId());
            }
        } else {
            if (!groupUser.isMentor()) {
                throw new DomainException("Người dùng đã là mentee");
            } else {
                groupUser.setMentor(false);
                groupUserRepository.save(groupUser);

                logger.info("User {} demoted to mentee in group {}", command.getUserId(), command.getGroupId());
            }
        }

        var refreshGroup = groupRepository.findById(command.getGroupId()).orElseThrow(() -> new DomainException("Không tìm thấy nhóm"));

        return modelMapper.map(refreshGroup, GroupDetailDto.class);
    }
}
