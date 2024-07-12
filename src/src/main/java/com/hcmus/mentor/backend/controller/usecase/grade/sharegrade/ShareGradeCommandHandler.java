package com.hcmus.mentor.backend.controller.usecase.grade.sharegrade;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
import com.hcmus.mentor.backend.controller.usecase.grade.common.GradeUserDto;
import com.hcmus.mentor.backend.domain.GradeUserAccess;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.GradeShareType;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.PermissionService;
import com.hcmus.mentor.backend.service.impl.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ShareGradeCommandHandler implements Command.Handler<ShareGradeCommand, GradeUserDto> {

    private final LoggedUserAccessor loggedUserAccessor;
    private final UserRepository userRepository;
    private final PermissionService permissionService;
    private final GradeService gradeService;

    @Override
    public GradeUserDto handle(ShareGradeCommand command) {
        var currentUser = getCurrentUser();
        validateAdminPermission(currentUser);

        var user = getUserById(command.getUserId());

        updateUserGradeShareType(user, command.getShareType());
        updateUserGradeAccess(user, command.getUserAccessIds());

        userRepository.save(user);

        return gradeService.mapToGradeUserDto(user);
    }

    private User getCurrentUser() {
        return userRepository.findById(loggedUserAccessor.getCurrentUserId())
                .orElseThrow(() -> new DomainException("Không tìm thấy người dùng hiện tại"));
    }

    private void validateAdminPermission(User currentUser) {
        if (!permissionService.isAdmin(currentUser.getId(), 0)) {
            throw new ForbiddenException("Chỉ admin mới có thể chia sẻ điểm");
        }
    }

    private User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("Không tìm thấy người dùng"));
    }

    private void updateUserGradeShareType(User user, GradeShareType shareType) {
        user.setGradeShareType(shareType);
    }

    private void updateUserGradeAccess(User user, List<String> userAccessIds) {
        var gradeUserAccess = new ArrayList<>(user.getUserCanAccessGrade());
        removeUnlistedUserAccess(gradeUserAccess, userAccessIds);
        addNewUserAccess(gradeUserAccess, userAccessIds, user);
        user.setUserCanAccessGrade(new HashSet<>(gradeUserAccess));
    }

    private void removeUnlistedUserAccess(List<GradeUserAccess> gradeUserAccess, List<String> userAccessIds) {
        gradeUserAccess.removeIf(gua -> !userAccessIds.contains(gua.getUserAccess().getId()));
    }

    private void addNewUserAccess(List<GradeUserAccess> gradeUserAccess, List<String> userAccessIds, User user) {
        userAccessIds.stream()
                .filter(id -> gradeUserAccess.stream().noneMatch(gua -> gua.getUserAccess().getId().equals(id)))
                .forEach(id -> {
                    var abc = GradeUserAccess.builder()
                            .user(user)
                            .userAccess(getUserById(id))
                            .build();
                    gradeUserAccess.add(abc);
                });
    }
}