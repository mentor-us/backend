package com.hcmus.mentor.backend.web.controller;

import com.hcmus.mentor.backend.usercase.common.repository.UserRepository;
import com.hcmus.mentor.backend.usercase.common.service.ConfigurationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.logging.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Configuration", description = "REST API for Configuration")
@RestController
@RequestMapping("/api/configurations")
@SecurityRequirement(name = "bearer")
public class ConfigurationController {

  private final Logger logger = Logger.getLogger(ConfigurationController.class.getName());
  private final UserRepository userRepository;

  private final String EMAIL_SYSTEM = "email_system";

  private final ConfigurationService configurationService;

  public ConfigurationController(UserRepository userRepository,
      ConfigurationService configurationService) {
    this.userRepository = userRepository;
    this.configurationService = configurationService;
  }

  @GetMapping("/email")
  public String getEmailSystem() {
    return "";
  }
}
