package com.hcmus.mentor.backend.usercase.common.repository;

import com.hcmus.mentor.backend.entity.confiuration.Configuration;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConfigurationRepository extends MongoRepository<Configuration, String>{
    Optional<Configuration> findById(String Id);

    List<Configuration> findAll();

    boolean existsById(String id);



}
