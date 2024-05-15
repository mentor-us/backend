package com.hcmus.mentor.backend.service.impl;

import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.GroupUserRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.hcmus.mentor.backend.domain.constant.UserRole.ADMIN;
import static com.hcmus.mentor.backend.domain.constant.UserRole.SUPER_ADMIN;

@Service
@Transactional
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final GroupRepository groupRepository;
    private final GroupUserRepository groupUserRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    public boolean isAdmin(String id, int dummy) {
        return isSuperAdmin(id, dummy) || userRepository.existsByIdAndRolesContains(id, ADMIN);
    }

    public boolean isSuperAdmin(String id, int dummy) {
        return userRepository.existsByIdAndRolesContains(id, SUPER_ADMIN);
    }

    @Override
    public boolean isAdmin(String email) {
        return isSuperAdmin(email) || userRepository.existsByEmailAndRolesContains(email, ADMIN);
    }

    @Override
    public boolean isSuperAdmin(String email) {
        return userRepository.existsByEmailAndRolesContains(email, SUPER_ADMIN);
    }

    @Override
    public boolean isGroupCreatorByEmail(String email, String groupId) {
        return groupRepository.existsByCreatorEmailAndId(email, groupId);
    }

    @Override
    public boolean hasPermissionOnGroup(String email, String groupId) {
        return isSuperAdmin(email) || isGroupCreatorByEmail(email, groupId);
    }

    @Override
    public boolean isMemberByEmailInGroup(String email, String groupId) {
        return groupUserRepository.existsMemberByEmailAndGroupId(email, groupId);
    }

    @Override
    public boolean isMentorByEmailOfGroup(String email, String groupId) {
        return groupUserRepository.existsMentorByEmailAndGroupId(email, groupId, true);
    }

    @Override
    public boolean isMemberInGroup(String userId, String groupId) {
        return groupUserRepository.existsByUserIdAndGroupId(userId, groupId);
    }

    @Override
    public boolean isMemberInChannel(String channelId, String userId) {
        return channelRepository.existsByIdAndUserId(channelId, userId);
    }

    @Override
    public boolean isMentorInChannel(String channelId, String userId) {
        return channelRepository.existsMentorInChannel(channelId, userId);
    }

}