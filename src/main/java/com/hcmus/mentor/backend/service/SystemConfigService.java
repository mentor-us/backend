package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.service.dto.SystemConfigServiceDto;

public interface SystemConfigService {
    SystemConfigServiceDto listAll(String emailUser);

    SystemConfigServiceDto updateValue(String emailUser, String id, Object value);

    void add();
}
