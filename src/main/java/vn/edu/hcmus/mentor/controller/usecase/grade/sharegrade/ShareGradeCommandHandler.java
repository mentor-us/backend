package vn.edu.hcmus.mentor.controller.usecase.grade.sharegrade;

import an.awesome.pipelinr.Command;
import vn.edu.hcmus.mentor.controller.exception.DomainException;
import vn.edu.hcmus.mentor.controller.exception.ForbiddenException;
import vn.edu.hcmus.mentor.controller.usecase.grade.common.GradeUserDto;
import vn.edu.hcmus.mentor.domain.AuditRecord;
import vn.edu.hcmus.mentor.domain.GradeUserAccess;
import vn.edu.hcmus.mentor.domain.User;
import vn.edu.hcmus.mentor.domain.constant.ActionType;
import vn.edu.hcmus.mentor.domain.constant.DomainType;
import vn.edu.hcmus.mentor.domain.constant.GradeShareType;
import vn.edu.hcmus.mentor.repository.UserRepository;
import vn.edu.hcmus.mentor.security.principal.LoggedUserAccessor;
import vn.edu.hcmus.mentor.service.AuditRecordService;
import vn.edu.hcmus.mentor.service.PermissionService;
import vn.edu.hcmus.mentor.service.impl.GradeService;
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
    private final AuditRecordService auditRecordService;

    @Override
    public GradeUserDto handle(ShareGradeCommand command) {
        var currentUser = getCurrentUser();
        validateAdminPermission(currentUser);

        var user = getUserById(command.getUserId());

        updateUserGradeShareType(user, command.getShareType());
        updateUserGradeAccess(user, command.getUserAccessIds());

        userRepository.save(user);
        var userCanAccess = new StringBuilder();
        user.getUserCanAccessGrade().forEach(gua -> userCanAccess.append(gua.getUserAccess().getEmail()).append(", "));
        auditRecordService.save(AuditRecord.builder()
                .action(ActionType.UPDATED)
                .domain(DomainType.USER)
                .entityId(user.getId())
                .detail(String.format("Chia sẻ điểm cho người dùng %s với loại chia sẻ %S người có quyền truy cập: %s", user.getEmail(), user.getGradeShareType(), userCanAccess))
                .user(currentUser)
                .build());

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