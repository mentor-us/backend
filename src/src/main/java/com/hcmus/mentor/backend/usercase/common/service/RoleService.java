package com.hcmus.mentor.backend.usercase.common.service;

import com.hcmus.mentor.backend.payload.request.CreateRoleRequest;
import com.hcmus.mentor.backend.payload.request.UpdateRoleRequest;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public interface RoleService {
  RoleServiceReturn findAll(String emailUser);

  RoleServiceReturn findById(String emailUser, String id);

  RoleServiceReturn create(String emailUser, CreateRoleRequest request);

  RoleServiceReturn deleteMultiple(String emailUser, List<String> ids);

  RoleServiceReturn update(String emailUser, String id, UpdateRoleRequest request);

  @Getter
  @Setter
  @NoArgsConstructor
  public static class RoleServiceReturn {
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
