package com.hcmus.mentor.backend.service.query;

import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.service.PermissionService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GroupSpecification {
    public static Specification<Group> hasName(String name) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("name"), "%" + name + "%");
    }

    public static Specification<Group> getSpecification(String gruopName, String groupDescription, String Status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (gruopName != null && !gruopName.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + gruopName.toLowerCase() + "%"));
            }

            if (groupDescription != null && !groupDescription.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + groupDescription.toLowerCase() + "%"));
            }

            if (Status != null && !Status.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), Status));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));

        };
    }

    public static Specification<Group> withConditions(
            String name,
            String mentorEmail,
            String menteeEmail,
            String groupCategory,
            String status,
            Date timeStart1,
            Date timeEnd1,
            Date timeStart2,
            Date timeEnd2
    ) {
        return (Root<Group> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isEmpty()) {
                predicates.add(builder.like(builder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (mentorEmail != null && !mentorEmail.isEmpty()) {
                predicates.add(builder.equal(root.join("mentors").get("email"), mentorEmail));
            }

            if (menteeEmail != null && !menteeEmail.isEmpty()) {
                predicates.add(builder.equal(root.join("mentees").get("email"), menteeEmail));
            }

            if (groupCategory != null && !groupCategory.isEmpty()) {
                predicates.add(builder.equal(root.get("groupCategory"), groupCategory));
            }

            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }

            if (timeStart1 != null && timeEnd1 != null) {
                predicates.add(builder.between(root.get("timeStart"), timeStart1, timeEnd1));
            }

            if (timeStart2 != null && timeEnd2 != null) {
                predicates.add(builder.between(root.get("timeEnd"), timeStart2, timeEnd2));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Group> withSuperAdminCheck(String emailUser, UserRepository userRepository, PermissionService permissionService) {
        return (root, query, builder) -> {
            if (!StringUtils.isEmpty(emailUser) && !permissionService.isSuperAdmin(emailUser)) {
                return builder.equal(root.get("creator").get("email"), emailUser);
            }
            return null;
        };
    }
}
