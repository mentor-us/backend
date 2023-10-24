package com.hcmus.mentor.backend.usercase.common.repository;

import com.hcmus.mentor.backend.entity.GroupCategory;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GroupCategoryRepository extends MongoRepository<GroupCategory, String> {
  boolean existsByName(String name);

  GroupCategory findByName(String name);

  List<GroupCategory> findByIdIn(List<String> ids);

  List<GroupCategory> findAllByStatus(GroupCategory.Status status);
}
