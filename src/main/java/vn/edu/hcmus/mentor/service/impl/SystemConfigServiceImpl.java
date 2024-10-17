package vn.edu.hcmus.mentor.service.impl;

import vn.edu.hcmus.mentor.controller.payload.ReturnCodeConstants;
import vn.edu.hcmus.mentor.domain.SystemConfig;
import vn.edu.hcmus.mentor.repository.SystemConfigRepository;
import vn.edu.hcmus.mentor.service.PermissionService;
import vn.edu.hcmus.mentor.service.SystemConfigService;
import vn.edu.hcmus.mentor.service.dto.SystemConfigServiceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static vn.edu.hcmus.mentor.controller.payload.ReturnCodeConstants.INVALID_PERMISSION;
import static vn.edu.hcmus.mentor.controller.payload.ReturnCodeConstants.SUCCESS;
import static vn.edu.hcmus.mentor.controller.payload.ReturnCodeConstants.TASK_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;
    private final PermissionService permissionService;

    @Override
    public SystemConfigServiceDto listAll(String emailUser) {
        if (!permissionService.isAdminByEmail(emailUser)) {
            return new SystemConfigServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }
        List<SystemConfig> systemConfigs = systemConfigRepository.findAll();
        return new SystemConfigServiceDto(SUCCESS, null, systemConfigs);
    }

    @Override
    public SystemConfigServiceDto updateValue(String emailUser, String id, Object value) {
        Optional<SystemConfig> configOptional = systemConfigRepository.findById(id);
        if (configOptional.isEmpty()) {
            return new SystemConfigServiceDto(TASK_NOT_FOUND, "Not found system config", null);
        }
        SystemConfig config = configOptional.get();
        if (!permissionService.isAdminByEmail(emailUser)) {
            return new SystemConfigServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }
        if (!isValidType(value, config.getType())) {
            return new SystemConfigServiceDto(ReturnCodeConstants.SYSTEM_CONFIG_INVALID_TYPE, "Invalid type", value);
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
                        return new SystemConfigServiceDto(ReturnCodeConstants.SYSTEM_CONFIG_INVALID_DOMAIN, "Invalid domain", domain);
                    }
                }
                break;
            case "valid_max_year":
                if ((int) value < 0) {
                    return new SystemConfigServiceDto(ReturnCodeConstants.SYSTEM_CONFIG_INVALID_MAX_YEAR, "Invalid max year", value);
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