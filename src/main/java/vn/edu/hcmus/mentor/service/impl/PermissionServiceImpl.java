package vn.edu.hcmus.mentor.service.impl;

import vn.edu.hcmus.mentor.repository.ChannelRepository;
import vn.edu.hcmus.mentor.repository.GroupRepository;
import vn.edu.hcmus.mentor.repository.GroupUserRepository;
import vn.edu.hcmus.mentor.repository.UserRepository;
import vn.edu.hcmus.mentor.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static vn.edu.hcmus.mentor.domain.constant.UserRole.ADMIN;
import static vn.edu.hcmus.mentor.domain.constant.UserRole.SUPER_ADMIN;

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
    public boolean isAdminByEmail(String email) {
        return isSuperAdminByEmail(email) || userRepository.existsByEmailAndRolesContains(email, ADMIN);
    }

    @Override
    public boolean isSuperAdminByEmail(String email) {
        return userRepository.existsByEmailAndRolesContains(email, SUPER_ADMIN);
    }

    @Override
    public boolean isGroupCreatorByEmail(String email, String groupId) {
        return groupRepository.existsByCreatorEmailAndId(email, groupId);
    }

    @Override
    public boolean hasPermissionOnGroup(String email, String groupId) {
        return isSuperAdminByEmail(email) || isGroupCreatorByEmail(email, groupId);
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