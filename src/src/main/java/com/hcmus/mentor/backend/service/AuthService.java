package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.config.AppProperties;
import com.hcmus.mentor.backend.entity.RefreshToken;
import com.hcmus.mentor.backend.payload.request.AuthResponse;
import com.hcmus.mentor.backend.payload.request.RefreshTokenRequest;
import com.hcmus.mentor.backend.repository.RefreshTokenRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.TokenProvider;
import java.util.Date;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserRepository userRepository;

  private final RefreshTokenRepository refreshTokenRepository;

  private final TokenProvider tokenProvider;

  private final AppProperties appProperties;

  public AuthService(
      UserRepository userRepository,
      RefreshTokenRepository refreshTokenRepository,
      TokenProvider tokenProvider,
      AppProperties appProperties) {
    this.userRepository = userRepository;
    this.refreshTokenRepository = refreshTokenRepository;
    this.tokenProvider = tokenProvider;
    this.appProperties = appProperties;
  }

  public AuthResponse createToken(String userId) {
    Date now = new Date();
    Date expiryDate =
        new Date(now.getTime() + appProperties.getAuth().getRefreshTokenExpirationMsec());
    RefreshToken refreshToken =
        RefreshToken.builder()
            .refreshToken(UUID.randomUUID().toString())
            .userId(userId)
            .issuedAt(now)
            .expiryDate(expiryDate)
            .build();
    RefreshToken subscription = refreshTokenRepository.save(refreshToken);
    return AuthResponse.builder()
        .refreshToken(subscription.getRefreshToken())
        .accessToken(tokenProvider.createToken(userId))
        .build();
  }

  public String generateNewToken(RefreshTokenRequest request) {
    RefreshToken subscription =
        refreshTokenRepository.findFirstByRefreshTokenOrderByIssuedAtDesc(
            request.getRefreshToken());
    if (subscription == null) {
      return null;
    }

    if (subscription.getExpiryDate().before(new Date())) {
      refreshTokenRepository.deleteByRefreshToken(subscription.getRefreshToken());
      return null;
    }

    String userId = tokenProvider.getUserIdFromToken(request.getAccessToken());
    if (userId == null) {
      return null;
    }
    if (!subscription.getUserId().equals(userId)) {
      return null;
    }
    return tokenProvider.createToken(userId);
  }
}
