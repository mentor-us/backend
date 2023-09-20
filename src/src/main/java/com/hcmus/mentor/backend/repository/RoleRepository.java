package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.entity.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RoleRepository extends MongoRepository<Role, String> {
    boolean existsByName(String name);
    List<Role> findByIdIn(List<String> ids);
}
