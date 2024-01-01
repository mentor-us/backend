package com.hcmus.mentor.backend.usercase.common.service;

import com.hcmus.mentor.backend.entity.confiuration.Configuration;
import com.hcmus.mentor.backend.payload.request.CreateConfigurationRequest;
import com.hcmus.mentor.backend.payload.response.configuration.ConfigurationDetailResponse;
import com.hcmus.mentor.backend.web.infrastructure.security.UserPrincipal;
import java.util.List;

public interface ConfigurationService {
  ConfigurationDetailResponse get(String id);
  List<ConfigurationDetailResponse> getAllConfigurations();
  Configuration create(CreateConfigurationRequest request);

  boolean UpdateConfiguration(UserPrincipal user, String id, String value);

}
