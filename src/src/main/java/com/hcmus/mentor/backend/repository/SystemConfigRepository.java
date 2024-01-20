package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.SystemConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SystemConfigRepository extends MongoRepository<SystemConfig, String> {
    SystemConfig findByKey(String key);
}
