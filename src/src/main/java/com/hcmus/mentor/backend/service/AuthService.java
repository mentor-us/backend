package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.payload.request.AuthResponse;
import com.hcmus.mentor.backend.payload.request.RefreshTokenRequest;

public interface AuthService {
  AuthResponse createToken(String userId);

  String generateNewToken(RefreshTokenRequest request);
}
