package com.hcmus.mentor.backend.usercase.common.repository;

import com.hcmus.mentor.backend.entity.Role;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoleRepository extends MongoRepository<Role, String> {
  boolean existsByName(String name);

  List<Role> findByIdIn(List<String> ids);
}
