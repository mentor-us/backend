package vn.edu.hcmus.mentor.backend.service;

import vn.edu.hcmus.mentor.backend.service.dto.SystemConfigServiceDto;

public interface SystemConfigService {
    SystemConfigServiceDto listAll(String emailUser);

    SystemConfigServiceDto updateValue(String emailUser, String id, Object value);

    void add();
}
