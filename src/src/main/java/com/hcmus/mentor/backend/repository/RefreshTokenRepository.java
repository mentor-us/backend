package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.entity.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {

    RefreshToken findFirstByRefreshTokenOrderByIssuedAtDesc(String refreshToken);

    void deleteByRefreshToken(String refreshToken);
}
