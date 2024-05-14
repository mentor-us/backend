package com.hcmus.mentor.backend.controller.usecase.group.searchowngroups;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.group.common.GroupHomepageDto;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.GroupUser;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Handler for {@link SearchOwnGroupsQuery}.
 */
@Component
@RequiredArgsConstructor
public class SearchOwnGroupsQueryHandler implements Command.Handler<SearchOwnGroupsQuery, Page<GroupHomepageDto>> {

    private final Logger logger = LogManager.getLogger(SearchOwnGroupsQueryHandler.class);
    private final LoggedUserAccessor loggedUserAccessor;
    private final ModelMapper modelMapper;
    private final GroupRepository groupRepository;

    /**
     * @param command command to find own groups.
     * @return result of finding own groups.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<GroupHomepageDto> handle(SearchOwnGroupsQuery command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();
        command.setUserId(currentUserId);

        Pageable pageable = PageRequest.of(command.getPage(), command.getPageSize());

        Page<Group> groupsQuery = groupRepository.findAllByIsMember(currentUserId, pageable);
        var abc = groupsQuery.getContent();
        List<GroupHomepageDto> groups = abc.stream()
                .map(mapToGroupHomepageDto(currentUserId))
                .sorted(Comparator.comparing(GroupHomepageDto::getUpdatedDate).reversed())
                .toList();

        return new PageImpl<>(groups, pageable, groups.size());
    }

    private @NotNull Function<Group, GroupHomepageDto> mapToGroupHomepageDto(String currentUserId) {
        return group -> {
            GroupHomepageDto groupHomepageDto = modelMapper.map(group, GroupHomepageDto.class);

            groupHomepageDto.setRole(currentUserId);

            var isPinned = group.getGroupUsers().stream()
                    .filter(gu -> gu.getUser().getId().equals(currentUserId))
                    .findFirst()
                    .map(GroupUser::isPinned)
                    .orElse(false);
            groupHomepageDto.setPinned(isPinned);

            return groupHomepageDto;
        };
    }

    private Boolean getIsMentorStatus(String isMentor) {
        return isMentor.equals("mentor");
    }
}