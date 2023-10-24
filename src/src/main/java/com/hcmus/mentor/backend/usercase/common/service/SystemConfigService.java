package com.hcmus.mentor.backend.usercase.common.service;

public interface SystemConfigService {
  SystemConfigReturnService listAll(String emailUser);

  SystemConfigReturnService updateValue(String emailUser, String id, Object value);

  void add();
}
