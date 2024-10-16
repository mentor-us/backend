package com.hcmus.mentor.backend.controller.usecase.task.add;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.exception.NotFoundException;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.controller.payload.ReturnCodeConstants;
import com.hcmus.mentor.backend.domain.*;
import com.hcmus.mentor.backend.domain.method.IRemindable;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.ReminderRepository;
import com.hcmus.mentor.backend.repository.TaskRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AddTaskCommandHandler implements Command.Handler<AddTaskCommand, Task> {

    private final Logger logger = LoggerFactory.getLogger(AddTaskCommandHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final ChannelRepository channelRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final GroupService groupService;
    private final MessageService messageService;
    private final SocketIOService socketIOService;
    private final NotificationService notificationService;
    private final ReminderRepository reminderRepository;

    @Override
    @Transactional
    public Task handle(AddTaskCommand command) {
        var loggedUserId = loggedUserAccessor.getCurrentUserId();

        if (!channelRepository.existsById(command.getGroupId())) {
            logger.error("Add task : Not found channel with id {}", command.getGroupId());

            throw new DomainException("Không tìm thấy kênh với id " + command.getGroupId(), ReturnCodeConstants.TASK_NOT_FOUND_GROUP);
        }

        if (command.getTitle() == null || command.getTitle().isEmpty() || command.getDeadline() == null) {
            logger.error("Add task : Not enough required fields : title={}, deadline={}", command.getTitle(), command.getDeadline());

            throw new DomainException("Không đủ trường bắt buộc", ReturnCodeConstants.TASK_NOT_ENOUGH_FIELDS);
        }

        Task parentTask = null;
        if (command.getParentTask() != null) {
            parentTask = taskRepository.findById(command.getParentTask()).orElse(null);
            if (command.getParentTask() != null && parentTask == null && !command.getParentTask().isEmpty()) {
                logger.error("Add task : Not found parent task with id {}", command.getParentTask());

                throw new DomainException("Không tìm thấy công việc cha", ReturnCodeConstants.TASK_NOT_FOUND_PARENT_TASK);
            }
        }

        var channel = channelRepository.findById(command.getGroupId()).orElse(null);
        if (channel == null) {
            logger.error("Add task : Not found channel with id {}", command.getGroupId());

            throw new DomainException("Không tìm thấy kênh với id " + command.getGroupId(), ReturnCodeConstants.TASK_NOT_FOUND_GROUP);
        }

        var membersOfChannel = channel.getUsers();
        var memberIdsOfChannel = membersOfChannel.stream().map(User::getId).toList();
        if (!command.getUserIds().contains("*")) {
            for (String userId : command.getUserIds()) {
                if (!memberIdsOfChannel.contains(userId)) {
                    logger.error("Add task : Not found user with id {} in group {}", userId, command.getGroupId());

                    throw new DomainException("Không tìm thấy người dùng với id " + userId + " trong nhóm", ReturnCodeConstants.TASK_NOT_FOUND_USER_IN_GROUP);
                }
            }
        }

        List<User> userAssignees = command.getUserIds().contains("*") ? membersOfChannel : userRepository.findAllById(command.getUserIds());
        User assigner = userRepository.findById(loggedUserId).orElse(null);
        if (assigner == null) {
            logger.error("Add task : Not found user with id {}", loggedUserId);

            throw new NotFoundException("Không tìm thấy người dùng với id " + loggedUserId, ReturnCodeConstants.TASK_NOT_FOUND);
        }

        var task = Task.builder()
                .title(command.getTitle())
                .description(command.getDescription())
                .deadline(command.getDeadline())
                .group(channel)
                .assigner(assigner)
                .parentTask(parentTask)
                .build();
        var assignee = userAssignees.stream().map(user -> Assignee.builder().task(task).user(user).build()).toList();
        task.setAssignees(assignee);
        taskRepository.save(task);

        Message message = messageService.saveTaskMessage(task);
        groupService.pingGroup(command.getGroupId());

        MessageDetailResponse response = messageService.mappingToMessageDetailResponse(message, assigner.getId());
        socketIOService.sendBroadcastMessage(response, task.getGroup().getId());
        saveToReminder(task);
        notificationService.sendForTask(task);

        return task;
    }

    private void saveToReminder(IRemindable remindable) {
        Reminder reminder = remindable.toReminder();
        Task task = (Task) remindable;
        reminder.setRecipients(task.getAssignees().stream().map(Assignee::getUser).toList());
        reminder.setSubject("Bạn có 1 công việc sắp tới hạn");
        reminderRepository.save(reminder);
    }
}
