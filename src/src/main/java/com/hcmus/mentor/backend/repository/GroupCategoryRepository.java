package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.GroupCategory;

import java.util.List;

import com.hcmus.mentor.backend.domain.constant.GroupCategoryStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GroupCategoryRepository extends MongoRepository<GroupCategory, String> {
    boolean existsByName(String name);

    GroupCategory findByName(String name);

    List<GroupCategory> findByIdIn(List<String> ids);

    List<GroupCategory> findAllByStatus(GroupCategoryStatus status);
}
