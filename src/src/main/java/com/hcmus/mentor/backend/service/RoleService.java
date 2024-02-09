package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.controller.payload.request.CreateRoleRequest;
import com.hcmus.mentor.backend.controller.payload.request.UpdateRoleRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

public interface RoleService {
    RoleServiceReturn findAll(String emailUser);

    RoleServiceReturn findById(String emailUser, String id);

    RoleServiceReturn create(String emailUser, CreateRoleRequest request);

    RoleServiceReturn deleteMultiple(String emailUser, List<String> ids);

    RoleServiceReturn update(String emailUser, String id, UpdateRoleRequest request);

    @Getter
    @Setter
    @NoArgsConstructor
    class RoleServiceReturn {
        Integer returnCode;
        String message;
        Object data;

        public RoleServiceReturn(Integer returnCode, String message, Object data) {
            this.returnCode = returnCode;
            this.message = message;
            this.data = data;
        }
    }
}
