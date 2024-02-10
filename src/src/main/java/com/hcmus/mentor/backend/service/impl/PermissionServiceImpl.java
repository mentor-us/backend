package com.hcmus.mentor.backend.service.impl;

import static com.hcmus.mentor.backend.domain.constant.UserRole.ADMIN;
import static com.hcmus.mentor.backend.domain.constant.UserRole.SUPER_ADMIN;

import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.service.PermissionService;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    @Override
    public boolean isAdmin(String email) {
        return isSuperAdmin(email) || userRepository.existsByEmailAndRolesIn(email, ADMIN);
    }

    @Override
    public boolean isSuperAdmin(String email) {
        return userRepository.existsByEmailAndRolesIn(email, SUPER_ADMIN);
    }

    @Override
    public boolean isGroupCreator(String email, String groupId) {
        Optional<User> userWrapper = userRepository.findByEmail(email);
        if (!userWrapper.isPresent()) {
            return false;
        }
        String userId = userWrapper.get().getId();
        return groupRepository.existsByIdAndCreatorId(groupId, userId);
    }

    @Override
    public boolean hasPermissionOnGroup(String email, String groupId) {
        return isSuperAdmin(email) || isGroupCreator(email, groupId);
    }

    @Override
    public boolean isInGroup(String email, String groupId) {
        Optional<User> userWrapper = userRepository.findByEmail(email);
        if (!userWrapper.isPresent()) {
            return false;
        }
        String userId = userWrapper.get().getId();
        return isUserIdInGroup(userId, groupId);
    }

    @Override
    public boolean isMentor(String email, String groupId) {
        Optional<User> userWrapper = userRepository.findByEmail(email);
        if (!userWrapper.isPresent()) {
            return false;
        }
        String userId = userWrapper.get().getId();
        return groupRepository.existsByIdAndMentorsIn(groupId, userId);
    }

    @Override
    public boolean isUserIdInGroup(String userId, String groupId) {
        return groupRepository.existsByIdAndMentorsIn(groupId, userId)
                || groupRepository.existsByIdAndMenteesIn(groupId, userId);
    }
}
