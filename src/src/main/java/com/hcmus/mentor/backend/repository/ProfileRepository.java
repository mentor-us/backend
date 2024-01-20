package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Profile;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProfileRepository extends MongoRepository<Profile, String> {

    Optional<Profile> findByUserId(String userId);
}
