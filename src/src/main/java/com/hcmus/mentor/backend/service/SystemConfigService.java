package com.hcmus.mentor.backend.service;

public interface SystemConfigService {
    SystemConfigReturnService listAll(String emailUser);

    SystemConfigReturnService updateValue(String emailUser, String id, Object value);

    void add();
}
