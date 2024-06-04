package com.hcmus.mentor.backend.service.impl;

import com.hcmus.mentor.backend.domain.SystemConfig;
import com.hcmus.mentor.backend.repository.SystemConfigRepository;
import com.hcmus.mentor.backend.service.PermissionService;
import com.hcmus.mentor.backend.service.SystemConfigService;
import com.hcmus.mentor.backend.service.dto.SystemConfigServiceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hcmus.mentor.backend.controller.payload.returnCode.InvalidPermissionCode.INVALID_PERMISSION;
import static com.hcmus.mentor.backend.controller.payload.returnCode.SuccessCode.SUCCESS;
import static com.hcmus.mentor.backend.controller.payload.returnCode.SystemConfigReturnCode.*;
import static com.hcmus.mentor.backend.controller.payload.returnCode.TaskReturnCode.NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;
    private final PermissionService permissionService;

    @Override
    public SystemConfigServiceDto listAll(String emailUser) {
        if (!permissionService.isAdmin(emailUser)) {
            return new SystemConfigServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }
        List<SystemConfig> systemConfigs = systemConfigRepository.findAll();
        return new SystemConfigServiceDto(SUCCESS, null, systemConfigs);
    }

    @Override
    public SystemConfigServiceDto updateValue(String emailUser, String id, Object value) {
        Optional<SystemConfig> configOptional = systemConfigRepository.findById(id);
        if (configOptional.isEmpty()) {
            return new SystemConfigServiceDto(NOT_FOUND, "Not found system config", null);
        }
        SystemConfig config = configOptional.get();
        if (!permissionService.isAdmin(emailUser)) {
            return new SystemConfigServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }
        if (!isValidType(value, config.getType())) {
            return new SystemConfigServiceDto(INVALID_TYPE, "Invalid type", value);
        }
        SystemConfigServiceDto isValidValue = isValidValue(config.getKey(), value);
        if (!Objects.equals(isValidValue.getReturnCode(), SUCCESS)) {
            return isValidValue;
        }
        config.setValue((String) value);
        systemConfigRepository.save(config);

        return new SystemConfigServiceDto(SUCCESS, "", config);
    }

    private boolean isValidType(Object value, String type) {
        String valueType = String.valueOf(value.getClass()).split(" ")[1];
        return valueType.equals(type);
    }

    private SystemConfigServiceDto isValidValue(String key, Object value) {
        switch (key) {
            case "valid_domains":
                String[] domains = (String[]) value;
                for (String domain : domains) {
                    if (!isValidDomain(domain)) {
                        return new SystemConfigServiceDto(INVALID_DOMAIN, "Invalid domain", domain);
                    }
                }
                break;
            case "valid_max_year":
                if ((int) value < 0) {
                    return new SystemConfigServiceDto(INVALID_MAX_YEAR, "Invalid max year", value);
                }
                break;
            default:
                break;
        }
        return new SystemConfigServiceDto(SUCCESS, "", "");
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
        ArrayList<String> domains = new ArrayList<String>();
        domains.add("fit.hcmus.edu.vn");
        domains.add("student.hcmus.edu.vn");
        domains.add("fit.gmail.com.vn");

        SystemConfig validDomains = SystemConfig.builder()
                .name("Domain hợp lệ")
                .description("Các domain cho phép đăng nhập trên hệ thống")
                .type(String.valueOf(domains.getClass()).split(" ")[1])
                .key("valid_domain")
                .value(String.valueOf(domains))
                .build();
        systemConfigRepository.save(validDomains);

        Integer maxYear = 7;
        SystemConfig validMaxYear =
                SystemConfig.builder()
                        .name("Thời gian học tối đa")
                        .description("Thời gian học tối đa của sinh viên")
                        .type(String.valueOf(maxYear.getClass()).split(" ")[1])
                        .key("valid_max_year")
                        .value(String.valueOf(maxYear))
                        .build();
        systemConfigRepository.save(validMaxYear);
    }
}