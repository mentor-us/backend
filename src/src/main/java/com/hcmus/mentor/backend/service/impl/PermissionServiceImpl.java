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

    @Override
    public boolean isAdmin(String email) {
        return isSuperAdmin(email) || userRepository.existsByEmailAndRolesContains(email, ADMIN);
    }

    @Override
    public boolean isSuperAdmin(String email) {
        return userRepository.existsByEmailAndRolesContains(email, SUPER_ADMIN);
    }

    @Override
    public boolean isGroupCreator(String email, String groupId) {
        return groupRepository.existsByCreatorEmailAndId(email, groupId);
    }

    @Override
    public boolean hasPermissionOnGroup(String email, String groupId) {
        return isSuperAdmin(email) || isGroupCreator(email, groupId);
    }

    @Override
    public boolean isInGroup(String email, String groupId) {
        return groupUserRepository.existsMemberByEmailAndGroupId(email, groupId);
    }

    @Override
    public boolean isMentor(String email, String groupId) {
        return groupUserRepository.existsMentorByEmailAndGroupId(email, groupId, true);
    }

    @Override
    public boolean isUserIdInGroup(String userId, String groupId) {
        return groupUserRepository.existsByUserIdAndGroupId(userId, groupId);
    }

    /**
     * @param channelId
     * @param userId
     * @return
     */
    @Override
    public boolean isUserInChannel(String channelId, String userId) {
        return channelRepository.existsByIdAndUserId(userId, channelId);
    }
}