package com.hcmus.mentor.backend.repository;
import com.hcmus.mentor.backend.entity.GroupCategory;
import com.hcmus.mentor.backend.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GroupCategoryRepository extends MongoRepository<GroupCategory, String> {
    boolean existsByName(String name);
    GroupCategory findByName(String name);
    List<GroupCategory> findByIdIn(List<String> ids);
    List<GroupCategory> findAllByStatus(GroupCategory.Status status);

}
