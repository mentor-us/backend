package com.hcmus.mentor.backend.controller.usecase.group.getallgroups;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.usecase.group.common.BasicGroupDto;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.QGroup;
import com.hcmus.mentor.backend.domain.constant.ChannelStatus;
import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import com.hcmus.mentor.backend.domainservice.GroupDomainService;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QSort;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetAllGroupsQueryHandler implements Command.Handler<GetAllGroupsQuery, Page<BasicGroupDto>> {

    private final Logger logger = LoggerFactory.getLogger(GetAllGroupsQueryHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final PermissionService permissionService;
    private final GroupRepository groupRepository;
    private final GroupDomainService groupDomainService;
    private final UserRepository userRepository;

    @Override
    public Page<BasicGroupDto> handle(GetAllGroupsQuery query) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        if (!query.getType().equals("admin")) {
            return Page.empty();
        }

        Page<Group> groups;

        boolean isSuperAdmin = permissionService.isSuperAdmin(currentUserId, 0);

        Pageable pageRequest = PageRequest.of(query.getPage(), query.getPageSize(), new QSort(QGroup.group.createdDate.desc()));

        if (isSuperAdmin) {
            groups = groupRepository.findAllWithPagination(pageRequest);
        } else {
            var exists = userRepository.existsById(currentUserId);
            if (!exists) {
                throw new DomainException("Không tìm thấy người dùng đăng nhập hiện tại");
            }

            groups = groupRepository.findAllByCreatorId(pageRequest, currentUserId);
        }

        for (Group group : groups.getContent()) {
            if (group.getStatus() == GroupStatus.DISABLED || group.getStatus() == GroupStatus.DELETED) {
                continue;
            }

            var groupStatus = groupDomainService.getGroupStatus(group.getTimeStart(), group.getTimeEnd());
            if (!groupStatus.equals(group.getStatus())) {
                group.setStatus(groupStatus);
                groupRepository.save(group);
            }
        }

        return groups.map(group -> {
            var channels = group.getChannels().stream()
                    .filter(channel -> channel.getStatus() == ChannelStatus.ACTIVE)
                    .collect(Collectors.toSet());
            group.setChannels(channels);
            return modelMapper.map(group, BasicGroupDto.class);
        });
    }
}