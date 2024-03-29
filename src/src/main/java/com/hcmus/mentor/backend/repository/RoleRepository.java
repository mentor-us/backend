package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Role;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoleRepository extends MongoRepository<Role, String> {
    boolean existsByName(String name);

    List<Role> findByIdIn(List<String> ids);
}
