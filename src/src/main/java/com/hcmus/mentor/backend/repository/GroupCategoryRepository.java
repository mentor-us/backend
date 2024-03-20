package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.GroupCategory;
import com.hcmus.mentor.backend.domain.constant.GroupCategoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
//import org.springframework.data.mongodb.repository.MongoRepository;

public interface GroupCategoryRepository extends JpaRepository<GroupCategory, String> {
    boolean existsByName(String name);

    GroupCategory findByName(String name);

    List<GroupCategory> findByIdIn(List<String> ids);

    List<GroupCategory> findAllByStatus(GroupCategoryStatus status);
}
