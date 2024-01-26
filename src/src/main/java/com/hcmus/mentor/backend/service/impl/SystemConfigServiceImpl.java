package com.hcmus.mentor.backend.service.impl;

import static com.hcmus.mentor.backend.controller.payload.returnCode.InvalidPermissionCode.INVALID_PERMISSION;
import static com.hcmus.mentor.backend.controller.payload.returnCode.SuccessCode.SUCCESS;
import static com.hcmus.mentor.backend.controller.payload.returnCode.SystemConfigReturnCode.*;
import static com.hcmus.mentor.backend.controller.payload.returnCode.TaskReturnCode.NOT_FOUND;

import com.hcmus.mentor.backend.domain.SystemConfig;
import com.hcmus.mentor.backend.repository.SystemConfigRepository;
import com.hcmus.mentor.backend.service.PermissionService;
import com.hcmus.mentor.backend.service.SystemConfigReturnService;
import com.hcmus.mentor.backend.service.SystemConfigService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;
    private final PermissionService permissionService;

    @Override
    public SystemConfigReturnService listAll(String emailUser) {
        if (!permissionService.isAdmin(emailUser)) {
            return new SystemConfigReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }
        List<SystemConfig> systemConfigs = systemConfigRepository.findAll();
        return new SystemConfigReturnService(SUCCESS, null, systemConfigs);
    }

    @Override
    public SystemConfigReturnService updateValue(String emailUser, String id, Object value) {
        Optional<SystemConfig> configOptional = systemConfigRepository.findById(id);
        if (!configOptional.isPresent()) {
            return new SystemConfigReturnService(NOT_FOUND, "Not found system config", null);
        }
        SystemConfig config = configOptional.get();
        if (!permissionService.isAdmin(emailUser)) {
            return new SystemConfigReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }
        if (!isValidType(value, config.getType())) {
            return new SystemConfigReturnService(INVALID_TYPE, "Invalid type", value);
        }
        SystemConfigReturnService isValidValue = isValidValue(config.getKey(), value);
        if (isValidValue.getReturnCode() != SUCCESS) {
            return isValidValue;
        }
        config.setValue(value);
        systemConfigRepository.save(config);

        return new SystemConfigReturnService(SUCCESS, "", config);
    }

    private boolean isValidType(Object value, String type) {
        String valueType = String.valueOf(value.getClass()).split(" ")[1];
        return valueType.equals(type);
    }

    private SystemConfigReturnService isValidValue(String key, Object value) {
        switch (key) {
            case "valid_domains":
                String[] domains = (String[]) value;
                for (String domain : domains) {
                    if (!isValidDomain(domain)) {
                        return new SystemConfigReturnService(INVALID_DOMAIN, "Invalid domain", domain);
                    }
                }
            case "valid_max_year":
                if ((int) value < 0) {
                    return new SystemConfigReturnService(INVALID_MAX_YEAR, "Invalid max year", value);
                }
            default:
                break;
        }
        return new SystemConfigReturnService(SUCCESS, "", "");
    }

    private boolean isValidDomain(String domain) {
        Pattern VALID_DOMAIN_REGEX =
                Pattern.compile(
                        "^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\\\.)+[A-Za-z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = VALID_DOMAIN_REGEX.matcher(domain);
        return matcher.matches();
    }

    @Override
    public void add() {
        ArrayList domains = new ArrayList<String>();
        domains.add("fit.hcmus.edu.vn");
        domains.add("student.hcmus.edu.vn");
        domains.add("fit.gmail.com.vn");

        SystemConfig validDomains =
                SystemConfig.builder()
                        .name("Domain hợp lệ")
                        .description("Các domain cho phép đăng nhập trên hệ thống")
                        .type(String.valueOf(domains.getClass()).split(" ")[1])
                        .key("valid_domain")
                        .value(domains)
                        .build();
        systemConfigRepository.save(validDomains);

        Integer maxYear = 7;
        SystemConfig validMaxYear =
                SystemConfig.builder()
                        .name("Thời gian học tối đa")
                        .description("Thời gian học tối đa của sinh viên")
                        .type(String.valueOf(maxYear.getClass()).split(" ")[1])
                        .key("valid_max_year")
                        .value(maxYear)
                        .build();
        systemConfigRepository.save(validMaxYear);
    }
}
