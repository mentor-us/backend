package com.hcmus.mentor.backend.service;

import static com.hcmus.mentor.backend.payload.returnCode.InvalidPermissionCode.INVALID_PERMISSION;
import static com.hcmus.mentor.backend.payload.returnCode.RoleReturnCode.*;
import static com.hcmus.mentor.backend.payload.returnCode.SuccessCode.SUCCESS;

import com.hcmus.mentor.backend.entity.Role;
import com.hcmus.mentor.backend.entity.User;
import com.hcmus.mentor.backend.payload.request.CreateRoleRequest;
import com.hcmus.mentor.backend.payload.request.UpdateRoleRequest;
import com.hcmus.mentor.backend.repository.PermissionRepository;
import com.hcmus.mentor.backend.repository.RoleRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import java.util.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Service
public class RoleService {
  private final RoleRepository roleRepository;
  private final PermissionService permissionService;
  private final PermissionRepository permissionRepository;
  private final UserRepository userRepository;

  public RoleService(
      RoleRepository roleRepository,
      PermissionService permissionService,
      PermissionRepository permissionRepository,
      UserRepository userRepository) {
    this.roleRepository = roleRepository;
    this.permissionService = permissionService;
    this.permissionRepository = permissionRepository;
    this.userRepository = userRepository;
  }

  public RoleServiceReturn findAll(String emailUser) {
    if (!permissionService.isAdmin(emailUser)) {
      return new RoleServiceReturn(INVALID_PERMISSION, "Invalid permission", null);
    }
    List<Role> roles = roleRepository.findAll();
    return new RoleServiceReturn(SUCCESS, "", roles);
  }

  public RoleServiceReturn findById(String emailUser, String id) {
    if (!permissionService.isAdmin(emailUser)) {
      return new RoleServiceReturn(INVALID_PERMISSION, "Invalid permission", null);
    }
    Optional<Role> roleOptional = roleRepository.findById(id);
    if (!roleOptional.isPresent()) {
      return new RoleServiceReturn(NOT_FOUND, "Not found role", null);
    }
    return new RoleServiceReturn(SUCCESS, "", roleOptional.get());
  }

  public RoleServiceReturn create(String emailUser, CreateRoleRequest request) {
    if (!permissionService.isAdmin(emailUser)) {
      return new RoleServiceReturn(INVALID_PERMISSION, "Invalid permission", null);
    }
    if (roleRepository.existsByName(request.getName())) {
      return new RoleServiceReturn(DUPLICATE_ROLE, "Duplicate role", null);
    }
    List<String> permissions = request.getPermissions();

    List<String> notFoundIds = new ArrayList<>();
    for (String permission : permissions) {
      if (!permissionRepository.existsById(permission)) {
        notFoundIds.add(permission);
      }
    }
    if (!notFoundIds.isEmpty()) {
      return new RoleServiceReturn(NOT_FOUND_PERMISSION, "Not found permissions", notFoundIds);
    }

    Role role =
        Role.builder()
            .name(request.getName())
            .description(request.getDescription())
            .permissions(permissions)
            .build();
    roleRepository.save(role);
    return new RoleServiceReturn(SUCCESS, "", role);
  }

  public RoleServiceReturn deleteMultiple(String emailUser, List<String> ids) {
    if (!permissionService.isAdmin(emailUser)) {
      return new RoleServiceReturn(INVALID_PERMISSION, "Invalid permission", null);
    }
    List<String> notFoundIds = new ArrayList<>();
    for (String id : ids) {
      Optional<Role> roleOptional = roleRepository.findById(id);
      if (!roleOptional.isPresent()) {
        notFoundIds.add(id);
      }
    }
    if (!notFoundIds.isEmpty()) {
      return new RoleServiceReturn(NOT_FOUND, "Not found role", notFoundIds);
    }

    List<Role> roles = roleRepository.findByIdIn(ids);
    List<User> users = userRepository.findAllByRolesIn(ids);
    for (Role role : roles) {
      removeRoleFromUsers(users, role.getId());
    }

    roleRepository.deleteAllById(ids);
    return new RoleServiceReturn(SUCCESS, "", roles);
  }

  private void removeRoleFromUsers(List<User> users, String roleId) {
    for (User user : users) {
      List<User.Role> roles = user.getRoles();
      roles.remove(roleId);
      user.setRoles(roles);
      userRepository.save(user);
    }
  }

  public RoleServiceReturn update(String emailUser, String id, UpdateRoleRequest request) {
    if (!permissionService.isAdmin(emailUser)) {
      return new RoleServiceReturn(INVALID_PERMISSION, "Invalid permission", null);
    }
    if (!roleRepository.existsById(id)) {
      return new RoleServiceReturn(NOT_FOUND, "Not found role", id);
    }
    if (roleRepository.existsByName(request.getName())) {
      return new RoleServiceReturn(DUPLICATE_ROLE, "Duplicate role", null);
    }
    List<String> permissions = request.getPermissions();
    List<String> notFoundIds = new ArrayList<>();
    for (String permission : permissions) {
      if (!permissionRepository.existsById(permission)) {
        notFoundIds.add(permission);
      }
    }
    if (!notFoundIds.isEmpty()) {
      return new RoleServiceReturn(NOT_FOUND_PERMISSION, "Not found permissions", notFoundIds);
    }

    Role role = roleRepository.findById(id).get();
    role.update(request);
    roleRepository.save(role);

    return new RoleServiceReturn(SUCCESS, "", role);
  }

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
