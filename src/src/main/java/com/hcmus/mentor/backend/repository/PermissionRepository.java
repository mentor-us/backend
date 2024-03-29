package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PermissionRepository extends MongoRepository<Role, String> {
    boolean existsById(String id);
}
