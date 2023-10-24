package com.hcmus.mentor.backend.usercase.common.service;

public interface PermissionService {
  boolean isAdmin(String email);

  boolean isSuperAdmin(String email);

  boolean isGroupCreator(String email, String groupId);

  boolean hasPermissionOnGroup(String email, String groupId);

  boolean isInGroup(String email, String groupId);

  boolean isMentor(String email, String groupId);

  boolean isUserIdInGroup(String userId, String groupId);
}
