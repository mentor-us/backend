package vn.edu.hcmus.mentor.service;

import vn.edu.hcmus.mentor.service.dto.SystemConfigServiceDto;

public interface SystemConfigService {
    SystemConfigServiceDto listAll(String emailUser);

    SystemConfigServiceDto updateValue(String emailUser, String id, Object value);

    void add();
}
