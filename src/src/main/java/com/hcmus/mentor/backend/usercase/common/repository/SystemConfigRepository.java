package com.hcmus.mentor.backend.usercase.common.repository;

import com.hcmus.mentor.backend.entity.SystemConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SystemConfigRepository extends MongoRepository<SystemConfig, String> {
  SystemConfig findByKey(String key);
}
