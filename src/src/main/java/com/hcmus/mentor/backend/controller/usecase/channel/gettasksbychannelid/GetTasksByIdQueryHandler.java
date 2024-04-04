package com.hcmus.mentor.backend.controller.usecase.channel.gettasksbychannelid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
import com.hcmus.mentor.backend.controller.payload.response.tasks.TaskDetailResponseAssigner;
import com.hcmus.mentor.backend.controller.payload.response.tasks.TaskDetailResponseRole;
import com.hcmus.mentor.backend.controller.usecase.task.common.TaskDetailResultChannel;
import com.hcmus.mentor.backend.controller.usecase.task.common.TaskDetailResult;
import com.hcmus.mentor.backend.domain.Task;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.TaskStatus;
import com.hcmus.mentor.backend.domain.dto.AssigneeDto;
import com.hcmus.mentor.backend.repository.ChannelRepository;
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
public class GetTasksByIdQueryHandler implements Command.Handler<GetTasksByIdQuery, List<TaskDetailResult>> {

    private final LoggedUserAccessor loggedUserAccessor;
    private final TaskRepository taskRepository;
    private final ChannelRepository channelRepository;
    private final PermissionService permissionService;
    private final UserRepository userRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TaskDetailResult> handle(GetTasksByIdQuery query) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        if (!permissionService.isUserInChannel(query.getId(), currentUserId)) {
            throw new ForbiddenException("Bạn không có quyền truy cập");
        }

        List<Task> tasks = taskRepository.findByGroupId(query.getId());

        return tasks.stream()
                .map(task -> generateTaskDetailFromTask(currentUserId, task))
                .sorted(Comparator.comparing(TaskDetailResult::getCreatedDate).reversed())
                .toList();
    }

    private TaskDetailResult generateTaskDetailFromTask(String userId, Task task) {
        TaskDetailResponseAssigner assigner = userRepository
                .findById(task.getAssignerId())
                .map(TaskDetailResponseAssigner::from)
                .orElse(null);

        TaskDetailResultChannel groupInfo = channelRepository
                .findById(task.getGroupId())
                .map(TaskDetailResultChannel::from)
                .orElse(null);

        TaskDetailResponseRole role = permissionService.isMentor(userId, task.getGroupId())
                ? TaskDetailResponseRole.MENTOR
                : TaskDetailResponseRole.MENTEE;

        Optional<User> userWrapper = userRepository.findById(userId);
        if (userWrapper.isEmpty()) {
            return TaskDetailResult.from(task, assigner, groupInfo, role, null);
        }

        TaskStatus status = task.getAssigneeIds().stream()
                .filter(assignee -> assignee.getUserId().equals(userWrapper.get().getId()))
                .findFirst()
                .map(AssigneeDto::getStatus)
                .orElse(null);
        return TaskDetailResult.from(task, assigner, groupInfo, role, status);
    }
}
