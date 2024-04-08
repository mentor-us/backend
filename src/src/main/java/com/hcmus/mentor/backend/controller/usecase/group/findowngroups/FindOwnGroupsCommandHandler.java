package com.hcmus.mentor.backend.controller.usecase.group.findowngroups;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.payload.response.groups.GroupHomepageResponse;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.GroupUser;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.service.GroupService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Handler for {@link FindOwnGroupsCommand}.
 */
@Component
@RequiredArgsConstructor
public class FindOwnGroupsCommandHandler implements Command.Handler<FindOwnGroupsCommand, Page<GroupHomepageResponse>> {

    private final GroupRepository groupRepository;
    private final GroupService groupService;

    /**
     * @param command command to find own groups.
     * @return result of finding own groups.
     */
    @Override
    public Page<GroupHomepageResponse> handle(FindOwnGroupsCommand command) {
        Pageable pageable = Pageable.ofSize(command.getPageSize()).withPage(command.getPage());

        Specification<Group> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Group, GroupUser> groupUserJoin = root.join("groupUsers", JoinType.LEFT);
            predicates.add(cb.equal(groupUserJoin.get("userId"), command.getUserId()));
            if (command.getIsMentor() != null) {
                predicates.add(cb.equal(groupUserJoin.get("isMentor"), command.getIsMentor()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        var groups = groupRepository.findAll(spec, pageable);
        var result = groups.getContent().stream()
                .map(group -> {
                    Optional<GroupUser> groupUser = group.getGroupUsers().stream()
                            .filter(gu -> gu.getUser().getId().equals(command.getUserId()))
                            .findFirst();
                    String role = groupUser.map(gu -> gu.isMentor() ? "mentor" : "mentee").orElse("mentee");
                    boolean isPinned = groupUser.map(GroupUser::isPinned).orElse(false);
                    return new GroupHomepageResponse(group, role, isPinned);
                })
                .sorted(Comparator.comparing(GroupHomepageResponse::getUpdatedDate).reversed())
                .toList();
        return new PageImpl<>(result, pageable, groups.getNumberOfElements());
    }
}
