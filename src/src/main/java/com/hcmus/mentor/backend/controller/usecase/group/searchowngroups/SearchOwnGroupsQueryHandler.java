package com.hcmus.mentor.backend.controller.usecase.group.searchowngroups;

import an.awesome.pipelinr.Command;
import com.google.common.base.Strings;
import com.hcmus.mentor.backend.controller.usecase.group.common.GroupHomepageDto;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.GroupUser;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
    public Page<GroupHomepageDto> handle(SearchOwnGroupsQuery command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();
        command.setUserId(currentUserId);

        Specification<Group> specification = (root, criteriaQuery, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(builder.equal(root.join("groupUsers", JoinType.INNER).get("user").get("id"), command.getUserId()));

            if (!Strings.isNullOrEmpty(command.getIsMentor())) {
                var isMentor = getIsMentorStatus(command.getIsMentor());

                predicates.add(builder.equal(root.join("groupUsers", JoinType.INNER).get("isMentor"), isMentor));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
        Pageable pageable = PageRequest.of(command.getPage(), command.getPageSize(), Sort.by("updatedDate").descending());

        var groupsQuery = groupRepository.findAll(specification, pageable);

        var groups = groupsQuery.getContent().stream()
                .map(mapToGroupHomepageDto(currentUserId))
                .sorted(Comparator.comparing(GroupHomepageDto::getUpdatedDate).reversed())
                .toList();

        return new PageImpl<>(groups, pageable, groups.size());
    }

    private @NotNull Function<Group, GroupHomepageDto> mapToGroupHomepageDto(String currentUserId) {
        return group -> {
            var groupHomepageDto = modelMapper.map(group, GroupHomepageDto.class);

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