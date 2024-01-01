package com.hcmus.mentor.backend.usercase.common.service.impl;

import com.hcmus.mentor.backend.entity.confiuration.Configuration;
import com.hcmus.mentor.backend.payload.request.CreateConfigurationRequest;
import com.hcmus.mentor.backend.payload.response.configuration.ConfigurationDetailResponse;
import com.hcmus.mentor.backend.usercase.common.repository.ConfigurationRepository;
import com.hcmus.mentor.backend.usercase.common.service.ConfigurationService;
import com.hcmus.mentor.backend.web.infrastructure.security.UserPrincipal;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.Value;
import org.springframework.stereotype.Service;

@Service
@Data
public class ConfigurationServiceImpl implements ConfigurationService {
  private final ConfigurationRepository configurationRepository;
  @Value("${spring.mail.username}")
  private final String DEFAULT_CONFIG_VALUE = "default";

  @Override
  public ConfigurationDetailResponse get(String id) {
    Optional<Configuration> configuration = configurationRepository.findById(id);
    return configuration.map(value -> ConfigurationDetailResponse.builder()
        .id(value.getId())
        .name(value.getName())
        .value(value.getValue())
        .updatedAt(value.getUpdatedAt())
        .updatedBy(value.getUpdatedBy())
        .build()).orElse(null);
  }

  @Override
  public List<ConfigurationDetailResponse> getAllConfigurations() {
    List<Configuration> configurations = configurationRepository.findAll();
    return configurations.stream().map(value -> ConfigurationDetailResponse.builder()
        .id(value.getId())
        .name(value.getName())
        .value(value.getValue())
        .updatedAt(value.getUpdatedAt())
        .updatedBy(value.getUpdatedBy())
        .build()).toList();
  }

  @Override
  public Configuration create(CreateConfigurationRequest request) {
    Configuration configuration = Configuration.builder()
        .name(request.getName())
        .value(request.getValue())
        .updatedBy(null)
        .build();
    return configurationRepository.save(configuration);
  }

  @Override
  public boolean UpdateConfiguration(UserPrincipal user, String id, String value) {
    Optional<Configuration> configuration = configurationRepository.findById(id);
    if (configuration.isPresent()) {
      Configuration config = configuration.get();
      config.setValue(value);
      config.setUpdatedBy(user.getId());
      configurationRepository.save(config);
      return true;
    }
    return false;
  }
}
