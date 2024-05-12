package com.hcmus.mentor.backend.controller.usecase.group.serachgroups;

import an.awesome.pipelinr.Command;
import com.google.common.base.Strings;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
import com.hcmus.mentor.backend.controller.usecase.group.common.GroupDetailDto;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.PermissionService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SearchGroupsQueryHandler implements Command.Handler<SearchGroupsQuery, Page<GroupDetailDto>> {

    private final Logger logger = LoggerFactory.getLogger(SearchGroupsQueryHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final PermissionService permissionService;
    private final GroupRepository groupRepository;

    /**
     * @param query
     * @return
     */
    @Override
    public Page<GroupDetailDto> handle(SearchGroupsQuery query) {
        var curerntUserId = loggedUserAccessor.getCurrentUserId();

        if (!permissionService.isAdmin(curerntUserId, 0)) {
            throw new ForbiddenException("Không có quyền truy cập");
        }

        Specification<Group> specification = (root, criteriaQuery, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (!Strings.isNullOrEmpty(query.getName())) {
                predicates.add(builder.like(builder.lower(root.get("name")), "%" + query.getName().toLowerCase() + "%"));
            }

            if (!Strings.isNullOrEmpty(query.getMentorEmail())) {
                predicates.add(builder.equal(root.join("groupUsers").join("user").get("email"), query.getMentorEmail()));
                predicates.add(builder.equal(root.join("groupUsers").get("isMentor"), true));
            }

            if (!Strings.isNullOrEmpty(query.getMenteeEmail())) {
                predicates.add(builder.equal(root.join("groupUsers").join("user").get("email"), query.getMentorEmail()));
                predicates.add(builder.equal(root.join("groupUsers").get("isMentor"), false));
            }

            if (!Strings.isNullOrEmpty(query.getGroupCategory())) {
                predicates.add(builder.equal(root.join("groupCategory").get("id"), query.getGroupCategory()));
            }

            if (query.getStatus() != null) {
                predicates.add(builder.equal(root.get("status"), query.getStatus()));
            }

            if (query.getTimeStart1() != null && query.getTimeEnd1() != null) {
                predicates.add(builder.between(root.get("timeStart"), query.getTimeStart1(), query.getTimeEnd1()));
            }

            if (query.getTimeStart2() != null && query.getTimeEnd2() != null) {
                predicates.add(builder.between(root.get("timeEnd"), query.getTimeStart2(), query.getTimeEnd2()));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };

        var pageRequest = PageRequest.of(query.getPage(), query.getPageSize());
        var groups = groupRepository.findAll(specification, pageRequest);

        return new PageImpl<>(groups.getContent().stream().map(group -> modelMapper.map(group, GroupDetailDto.class)).toList(), pageRequest, groups.getTotalElements());
    }
}
