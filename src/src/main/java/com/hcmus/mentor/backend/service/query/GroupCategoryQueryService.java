package com.hcmus.mentor.backend.service.query;

import com.hcmus.mentor.backend.controller.payload.request.FindGroupCategoryRequest;
import com.hcmus.mentor.backend.domain.GroupCategory;

import com.hcmus.mentor.backend.repository.GroupCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GroupCategoryQueryService extends BaseQueryService<GroupCategory> {
    private final GroupCategoryRepository groupCategoryRepository;
    private final Logger log = LoggerFactory.getLogger(GroupCategoryQueryService.class);

    protected Specification<GroupCategory> createSpecification(FindGroupCategoryRequest request){
        Specification<GroupCategory> specification = Specification.where(null);
        if(request.getName() != null){
            specification = specification.and(likeUpperSpecification(root -> root.get("name"), request.getName()));
        }
        if(request.getDescription() != null){
            specification = specification.and(likeUpperSpecification(root -> root.get("description"), request.getDescription()));
        }
//        if(request.getStatus() != null){
//            specification = specification.and(buildSpecification( "status"));
//        }
        return specification;
    }
}
