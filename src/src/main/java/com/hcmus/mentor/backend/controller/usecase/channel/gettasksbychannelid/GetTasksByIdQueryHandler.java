package com.hcmus.mentor.backend.controller.usecase.channel.gettasksbychannelid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
import com.hcmus.mentor.backend.controller.payload.response.tasks.TaskDetailResponse;
import com.hcmus.mentor.backend.domain.Task;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.TaskStatus;
import com.hcmus.mentor.backend.domain.dto.AssigneeDto;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.TaskRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GetTasksByIdQueryHandler implements Command.Handler<GetTasksByIdQuery, List<TaskDetailResponse>> {

    private final LoggedUserAccessor loggedUserAccessor;
    private final TaskRepository taskRepository;
    private final GroupRepository groupRepository;
    private final PermissionService permissionService;
    private final UserRepository userRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TaskDetailResponse> handle(GetTasksByIdQuery query) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        if (!permissionService.isUserInChannel(query.getId(), currentUserId)) {
            throw new ForbiddenException("Bạn không có quyền truy cập");
        }

        List<Task> tasks = taskRepository.findByGroupId(query.getId());

        return tasks.stream()
                .map(task -> generateTaskDetailFromTask(currentUserId, task))
                .sorted(Comparator.comparing(TaskDetailResponse::getCreatedDate).reversed())
                .toList();
    }

    private TaskDetailResponse generateTaskDetailFromTask(String userId, Task task) {
        TaskDetailResponse.Assigner assigner = userRepository
                .findById(task.getAssignerId())
                .map(TaskDetailResponse.Assigner::from)
                .orElse(null);

        TaskDetailResponse.Group groupInfo = groupRepository
                .findById(task.getGroupId())
                .map(TaskDetailResponse.Group::from)
                .orElse(null);

        TaskDetailResponse.Role role = permissionService.isMentor(userId, task.getGroupId())
                ? TaskDetailResponse.Role.MENTOR
                : TaskDetailResponse.Role.MENTEE;

        Optional<User> userWrapper = userRepository.findById(userId);
        if (userWrapper.isEmpty()) {
            return TaskDetailResponse.from(task, assigner, groupInfo, role, null);
        }

        TaskStatus status = task.getAssigneeIds().stream()
                .filter(assignee -> assignee.getUserId().equals(userWrapper.get().getId()))
                .findFirst()
                .map(AssigneeDto::getStatus)
                .orElse(null);
        return TaskDetailResponse.from(task, assigner, groupInfo, role, status);
    }
}
