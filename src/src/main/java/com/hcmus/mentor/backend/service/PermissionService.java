package com.hcmus.mentor.backend.service;

public interface PermissionService {
    boolean isAdmin(String id, int dummy);

    boolean isSuperAdmin(String id, int dummy);

    boolean isAdmin(String email);

    boolean isSuperAdmin(String email);

    boolean isGroupCreatorByEmail(String email, String groupId);

    boolean hasPermissionOnGroup(String email, String groupId);

    boolean isMentorByEmailOfGroup(String email, String groupId);

    boolean isMemberInGroup(String userId, String groupId);
    boolean isMemberByEmailInGroup(String email, String groupId);

    boolean isMemberInChannel(String channelId, String userId);
    boolean isMentorInChannel(String channelId, String userId);
}