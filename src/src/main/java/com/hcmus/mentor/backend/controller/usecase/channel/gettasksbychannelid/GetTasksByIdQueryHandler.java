package com.hcmus.mentor.backend.controller.usecase.channel.gettasksbychannelid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
import com.hcmus.mentor.backend.controller.payload.response.tasks.TaskDetailResponseAssigner;
import com.hcmus.mentor.backend.controller.payload.response.tasks.TaskDetailResponseRole;
import com.hcmus.mentor.backend.controller.usecase.task.common.TaskDetailResult;
import com.hcmus.mentor.backend.controller.usecase.task.common.TaskDetailResultChannel;
import com.hcmus.mentor.backend.domain.Assignee;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.Task;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.ChannelStatus;
import com.hcmus.mentor.backend.domain.constant.TaskStatus;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.TaskRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
    private final GroupRepository groupRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TaskDetailResult> handle(GetTasksByIdQuery query) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        List<String> channelIds = new ArrayList<>();
        if (permissionService.isMemberInChannel(query.getId(), currentUserId)) {
            channelIds.add(query.getId());

        } else {
            if (!permissionService.isMemberInGroup(currentUserId, query.getId())) {
                throw new ForbiddenException("Bạn không có quyền truy cập");
            }
            var group = groupRepository.findById(query.getId()).orElseThrow();
            channelIds = group.getChannels().stream()
                    .filter(channel -> channel.getStatus() == ChannelStatus.ACTIVE)
                    .map(Channel::getId).toList();
        }

        List<Task> tasks = taskRepository.findAllByGroupIdIn(channelIds);

        return tasks.stream()
                .map(task -> generateTaskDetailFromTask(currentUserId, task))
                .sorted(Comparator.comparing(TaskDetailResult::getCreatedDate).reversed())
                .toList();
    }

    private TaskDetailResult generateTaskDetailFromTask(String userId, Task task) {
        TaskDetailResponseAssigner assigner = TaskDetailResponseAssigner.from(task.getAssigner());

        TaskDetailResultChannel groupInfo = TaskDetailResultChannel.from(task.getGroup());

        TaskDetailResponseRole role = permissionService.isMentorByEmailOfGroup(userId, task.getGroup().getGroup().getId())
                ? TaskDetailResponseRole.MENTOR
                : TaskDetailResponseRole.MENTEE;

        Optional<User> userWrapper = userRepository.findById(userId);
        if (userWrapper.isEmpty()) {
            return TaskDetailResult.from(task, assigner, groupInfo, role, null);
        }

        TaskStatus status = task.getAssignees().stream()
                .filter(assignee -> assignee.getUser().getId().equals(userWrapper.get().getId()))
                .findFirst()
                .map(Assignee::getStatus)
                .orElse(null);
        return TaskDetailResult.from(task, assigner, groupInfo, role, status);
    }
}