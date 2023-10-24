package com.hcmus.mentor.backend.usercase.common.repository;

import com.hcmus.mentor.backend.entity.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PermissionRepository extends MongoRepository<Role, String> {
  boolean existsById(String id);
}
