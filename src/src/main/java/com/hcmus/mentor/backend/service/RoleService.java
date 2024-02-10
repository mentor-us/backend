package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.controller.payload.request.CreateRoleRequest;
import com.hcmus.mentor.backend.controller.payload.request.UpdateRoleRequest;
import com.hcmus.mentor.backend.service.dto.RoleServiceDto;

import java.util.List;

public interface RoleService {
    RoleServiceDto findAll(String emailUser);

    RoleServiceDto findById(String emailUser, String id);

    RoleServiceDto create(String emailUser, CreateRoleRequest request);

    RoleServiceDto deleteMultiple(String emailUser, List<String> ids);

    RoleServiceDto update(String emailUser, String id, UpdateRoleRequest request);

}
