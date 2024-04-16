package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, String> {
    SystemConfig findByKey(String key);
}
