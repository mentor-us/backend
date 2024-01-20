package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {

    RefreshToken findFirstByRefreshTokenOrderByIssuedAtDesc(String refreshToken);

    void deleteByRefreshToken(String refreshToken);
}
