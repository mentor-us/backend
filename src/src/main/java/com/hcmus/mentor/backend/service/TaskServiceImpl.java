package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.controller.payload.request.AddTaskRequest;
import com.hcmus.mentor.backend.controller.payload.request.UpdateStatusByMentorRequest;
import com.hcmus.mentor.backend.controller.payload.request.UpdateTaskRequest;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageResponse;
import com.hcmus.mentor.backend.controller.payload.response.tasks.TaskAssigneeResponse;
import com.hcmus.mentor.backend.controller.payload.response.tasks.TaskDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.tasks.TaskResponse;
import com.hcmus.mentor.backend.controller.payload.response.users.ProfileResponse;
import com.hcmus.mentor.backend.domain.*;
import com.hcmus.mentor.backend.domain.constant.ChannelStatus;
import com.hcmus.mentor.backend.domain.constant.TaskStatus;
import com.hcmus.mentor.backend.domain.dto.AssigneeDto;
import com.hcmus.mentor.backend.domain.method.IRemindable;
import com.hcmus.mentor.backend.repository.*;
import com.hcmus.mentor.backend.security.principal.userdetails.CustomerUserDetails;
import com.hcmus.mentor.backend.util.DateUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.hcmus.mentor.backend.controller.payload.returnCode.InvalidPermissionCode.INVALID_PERMISSION;
import static com.hcmus.mentor.backend.controller.payload.returnCode.SuccessCode.SUCCESS;
import static com.hcmus.mentor.backend.controller.payload.returnCode.TaskReturnCode.*;
import static com.hcmus.mentor.backend.service.impl.MessageServiceImpl.getTaskAssigneeResponses;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements IRemindableService {

    private final TaskRepository taskRepository;
    private final PermissionService permissionService;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;
    private final GroupService groupService;
    private final MessageService messageService;
    private final SocketIOService socketIOService;
    private final ReminderRepository reminderRepository;
    private final NotificationService notificationService;
    private final ChannelRepository channelRepository;

    public TaskReturnService addTask(String emailUser, AddTaskRequest request) {
        var user = userRepository.findByEmail(emailUser).orElse(null);
        if (user == null) {
            return new TaskReturnService(NOT_FOUND, "Not found user", null);
        }

        var channel = channelRepository.findById(request.getChannelId()).orElse(null);
        if (channel == null) {
            return new TaskReturnService(NOT_FOUND, "Not found channel", null);
        }
        if (!channel.isMember(user.getId())) {
            return new TaskReturnService(INVALID_PERMISSION, "User is not in channel", null);
        }

        List<String> userIds = request.getUserIds().contains("*")
                ? channel.getUserIds()
                : request.getUserIds();
        Optional<User> assigner = userRepository.findByEmail(emailUser);
        List<AssigneeDto> assigneeIds = userIds.stream().map(Task::newTask).toList();
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .deadline(request.getDeadline())
                .channelId(request.getChannelId())
                .assignerId(assigner.map(User::getId).orElse(null))
                .parentTask(request.getParentTask())
                .assigneeIds(assigneeIds)
                .build();
        taskRepository.save(task);

        Message message = Message.builder()
                .senderId(task.getAssignerId())
                .channelId(task.getChannelId())
                .createdDate(task.getCreatedDate())
                .type(Message.Type.TASK)
                .taskId(task.getId())
                .build();
        messageService.saveMessage(message);

        MessageDetailResponse response =
                messageService.fulfillTaskMessage(
                        MessageResponse.from(message, ProfileResponse.from(assigner.get())));
        socketIOService.sendBroadcastMessage(response, task.getChannelId());
        saveToReminder(task);
        notificationService.sendNewTaskNotification(response);

        return new TaskReturnService(SUCCESS, "", task);
    }

    public TaskReturnService getTasksByChannelId(String emailUser, String channelId) {
        if (!groupRepository.existsById(channelId)) {
            return new TaskReturnService(NOT_FOUND_GROUP, "Not found group", null);
        }
        if (!permissionService.isInGroup(emailUser, channelId)) {
            return new TaskReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }
        List<Task> tasks = taskRepository.findByChannelId(channelId);
        List<TaskDetailResponse> taskDetailResponses =
                tasks.stream()
                        .map(task -> generateTaskDetailFromTask(emailUser, task))
                        .sorted(Comparator.comparing(TaskDetailResponse::getCreatedDate).reversed())
                        .toList();
        return new TaskReturnService(SUCCESS, "", taskDetailResponses);
    }

    public TaskReturnService getTasksByEmailUser(String emailUser) {
        Optional<User> userWrapper = userRepository.findByEmail(emailUser);
        if (!userWrapper.isPresent()) {
            return new TaskReturnService(NOT_FOUND_USER_IN_GROUP, "Not found user in group", null);
        }
        User user = userWrapper.get();
        List<Task> tasks = taskRepository.findByAssigneeIdsUserId(user.getId());
        List<TaskDetailResponse> taskDetailResponses = new ArrayList<>();
        for (Task task : tasks) {
            TaskDetailResponse taskDetailResponse = generateTaskDetailFromTask(emailUser, task);
            taskDetailResponses.add(taskDetailResponse);
        }
        return new TaskReturnService(SUCCESS, "", taskDetailResponses);
    }

    public TaskReturnService deleteTask(CustomerUserDetails user, String id) {
        Optional<Task> taskOptional = taskRepository.findById(id);
        if (!taskOptional.isPresent()) {
            return new TaskReturnService(NOT_FOUND, "Not found task", null);
        }
        Task task = taskOptional.get();
        if (!permissionService.isMentor(user.getEmail(), task.getChannelId())
                && !task.getAssignerId().equals(user.getId())) {
            return new TaskReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }

        taskRepository.delete(task);
        reminderRepository.deleteByRemindableId(task.getId());

        List<Task> childrenTasks = taskRepository.findAllByParentTask(id);
        childrenTasks.forEach(
                childrenTask -> {
                    task.setParentTask(null);
                    taskRepository.save(childrenTask);
                });

        return new TaskReturnService(SUCCESS, "", task);
    }

    private TaskDetailResponse generateTaskDetailFromTask(String emailUser, Task task) {
        TaskDetailResponse.Assigner assigner =
                userRepository
                        .findById(task.getAssignerId())
                        .map(TaskDetailResponse.Assigner::from)
                        .orElse(null);

        TaskDetailResponse.Group groupInfo =
                groupRepository
                        .findById(task.getChannelId())
                        .map(TaskDetailResponse.Group::from)
                        .orElse(null);

        TaskDetailResponse.Role role =
                permissionService.isMentor(emailUser, task.getChannelId())
                        ? TaskDetailResponse.Role.MENTOR
                        : TaskDetailResponse.Role.MENTEE;

        Optional<User> userWrapper = userRepository.findByEmail(emailUser);
        if (!userWrapper.isPresent()) {
            return TaskDetailResponse.from(task, assigner, groupInfo, role, null);
        }

        TaskStatus status =
                task.getAssigneeIds().stream()
                        .filter(assignee -> assignee.getUserId().equals(userWrapper.get().getId()))
                        .findFirst()
                        .map(AssigneeDto::getStatus)
                        .orElse(null);
        return TaskDetailResponse.from(task, assigner, groupInfo, role, status);
    }

    public TaskReturnService getTask(String emailUser, String id) {
        Optional<Task> taskOptional = taskRepository.findById(id);
        if (!taskOptional.isPresent()) {
            return new TaskReturnService(NOT_FOUND, "Not found task", null);
        }
        Task task = taskOptional.get();
        if (!permissionService.isInGroup(emailUser, task.getChannelId())) {
            return new TaskReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }
        TaskDetailResponse taskDetailResponse = generateTaskDetailFromTask(emailUser, task);
        return new TaskReturnService(SUCCESS, "", taskDetailResponse);
    }

    public TaskReturnService getTaskAssigner(String emailUser, String id) {
        Optional<Task> taskOptional = taskRepository.findById(id);
        if (!taskOptional.isPresent()) {
            return new TaskReturnService(NOT_FOUND, "Not found task", null);
        }
        Task task = taskOptional.get();
        if (!permissionService.isInGroup(emailUser, task.getChannelId())) {
            return new TaskReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }
        User user = userRepository.findById(task.getAssignerId()).get();
        ProfileResponse response = ProfileResponse.from(user);
        return new TaskReturnService(SUCCESS, "", response);
    }

    public TaskReturnService getTaskAssigneesWrapper(String emailUser, String id) {
        Optional<Task> taskOptional = taskRepository.findById(id);
        if (!taskOptional.isPresent()) {
            return new TaskReturnService(NOT_FOUND, "Not found task", null);
        }

        Task task = taskOptional.get();
        if (!permissionService.isInGroup(emailUser, task.getChannelId())) {
            return new TaskReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }

        Optional<Group> groupWrapper = groupRepository.findById(task.getChannelId());
        if (!groupWrapper.isPresent()) {
            return new TaskReturnService(NOT_FOUND, "Not found group", null);
        }
        return new TaskReturnService(SUCCESS, "", getTaskAssignees(task.getId()));
    }

    public List<TaskAssigneeResponse> getTaskAssignees(String taskId) {
        return getTaskAssigneeResponses(taskId, taskRepository, groupRepository, userRepository);
    }

    private boolean isAssigned(String emailUser, Task task) {
        Optional<User> userOptional = userRepository.findByEmail(emailUser);
        if (!userOptional.isPresent()) {
            return false;
        }
        User user = userOptional.get();
        return taskRepository.existsByIdAndAssigneeIdsUserId(task.getId(), user.getId());
    }

    public TaskReturnService updateStatus(String emailUser, String id, TaskStatus status) {
        Optional<Task> taskOptional = taskRepository.findById(id);
        if (!taskOptional.isPresent()) {
            return new TaskReturnService(NOT_FOUND, "Not found task", null);
        }
        Task task = taskOptional.get();
        if (!isAssigned(emailUser, task)) {
            return new TaskReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }
        Optional<User> userOptional = userRepository.findByEmail(emailUser);
        if (!userOptional.isPresent()) {
            return new TaskReturnService(NOT_FOUND, "Not found user", null);
        }
        User user = userOptional.get();
        task.getAssigneeIds().stream()
                .filter(assignee -> assignee.getUserId().equals(user.getId()))
                .forEach(assignee -> assignee.setStatus(status));
        taskRepository.save(task);
        groupService.pingGroup(task.getChannelId());

        return new TaskReturnService(SUCCESS, "", task);
    }

    public TaskReturnService updateStatusByMentor(
            String emailUser, String id, UpdateStatusByMentorRequest request) {
        Optional<Task> taskOptional = taskRepository.findById(id);
        if (!taskOptional.isPresent()) {
            return new TaskReturnService(NOT_FOUND, "Not found task", null);
        }
        Task task = taskOptional.get();
        if (!permissionService.isMentor(emailUser, task.getChannelId())) {
            return new TaskReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }
        if (!isAssigned(request.getEmailUserAssigned(), task)) {
            return new TaskReturnService(NOT_FOUND, "Not found task", null);
        }
        Optional<User> userOptional = userRepository.findByEmail(request.getEmailUserAssigned());
        if (!userOptional.isPresent()) {
            return new TaskReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }
        User user = userOptional.get();
        task.getAssigneeIds().stream()
                .filter(assignee -> assignee.getUserId().equals(user.getId()))
                .forEach(assignee -> assignee.setStatus(request.getStatus()));
        taskRepository.save(task);
        groupService.pingGroup(task.getChannelId());

        return new TaskReturnService(SUCCESS, "", task);
    }

    public TaskReturnService updateTask(CustomerUserDetails user, String id, UpdateTaskRequest request) {
        Optional<Task> taskOptional = taskRepository.findById(id);
        if (!taskOptional.isPresent()) {
            return new TaskReturnService(NOT_FOUND, "Not found task", null);
        }
        Task task = taskOptional.get();
        if (!permissionService.isMentor(user.getEmail(), task.getChannelId())
                && !task.getAssignerId().equals(user.getId())) {
            return new TaskReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }
        if (request.getParentTask() != null && !taskRepository.existsById(request.getParentTask())) {
            return new TaskReturnService(NOT_FOUND_PARENT_TASK, "Not found parent task", null);
        }
        for (String assigneeId : request.getUserIds()) {
            if (!permissionService.isUserIdInGroup(assigneeId, task.getChannelId())) {
                return new TaskReturnService(NOT_FOUND_USER_IN_GROUP, "Not found user in group", null);
            }
        }

        task.update(request);
        taskRepository.save(task);

        Reminder reminder = reminderRepository.findByRemindableId(task.getId());
        if (reminder != null) {
            reminder.setReminderDate(task.getReminderDate());
            reminderRepository.save(reminder);
        }

        groupService.pingGroup(task.getChannelId());

        return new TaskReturnService(SUCCESS, "", task);
    }

    public List<TaskResponse> getMostRecentTasks(String userId) {
        List<String> groupIds = groupService.getAllActiveOwnGroups(userId).stream()
                .map(Group::getId)
                .toList();
        var channelIds = channelRepository.findByParentIdInAndStatus(groupIds, ChannelStatus.ACTIVE).stream()
                .map(Channel::getId)
                .toList();

        List<Task> tasks =
                taskRepository
                        .findAllByChannelIdInAndAssigneeIdsUserIdInAndDeadlineGreaterThan(
                                channelIds,
                                Arrays.asList("*", userId),
                                new Date(),
                                PageRequest.of(0, 5, Sort.by("deadline").descending()))
                        .getContent();
        return tasks.stream()
                .map(task -> {
                    Group group = groupRepository.findByChannelIdsContains(task.getChannelId());
                    User assigner = userRepository.findById(task.getAssignerId()).orElse(null);
                    AssigneeDto assignee = task.getAssigneeIds().stream()
                            .filter(a -> a.getUserId().equals(userId))
                            .findFirst()
                            .orElse(null);
                    return TaskResponse.from(task, assigner, assignee, group);
                })
                .toList();
    }

    public List<Task> getAllOwnTasks(String userId) {
        var joinedChannelIds = channelRepository.findByUserIdsContainingAndStatusIs(userId, ChannelStatus.ACTIVE).stream()
                .map(Channel::getId)
                .toList();

        return taskRepository.findAllByChannelIdInAndAssigneeIdsUserIdIn(joinedChannelIds, Arrays.asList("*", userId));
    }

    public List<Task> getAllOwnTaskByDate(String userId, Date date) {
        Date startTime = DateUtils.atStartOfDay(date);
        Date endTime = DateUtils.atEndOfDay(date);
        return getAllOwnTasksBetween(userId, startTime, endTime);
    }

    public List<Task> getAllOwnTasksBetween(String userId, Date startTime, Date endTime) {
        var joinedChannelIds = channelRepository.findByUserIdsContainingAndStatusIs(userId, ChannelStatus.ACTIVE).stream()
                .map(Channel::getId)
                .toList();
        MatchOperation match = Aggregation.match(
                Criteria.where("channelId")
                        .in(joinedChannelIds)
                        .and("assigneeIds.userId")
                        .in(Arrays.asList("*", userId))
                        .and("deadline")
                        .gte(startTime)
                        .lte(endTime));
        Aggregation aggregation = Aggregation.newAggregation(match);
        return mongoTemplate.aggregate(aggregation, "task", Task.class).getMappedResults();
    }

    public List<Task> getAllOwnTasksByMonth(String userId, Date date) {
        Date startTime = DateUtils.atStartOfMonth(date);
        Date endTime = DateUtils.atEndOfMonth(date);
        return getAllOwnTasksBetween(userId, startTime, endTime);
    }

    public TaskServiceImpl.TaskReturnService getAllOwnTasks(String channelId, String userId) {
        var resultValidate = validateChannel(channelId, userId);
        if (!resultValidate.getReturnCode().equals(SUCCESS)) {
            return resultValidate;
        }

        List<TaskResponse> assignedTask = getOwnAssignedTasks(channelId, userId);
        List<TaskResponse> assignedByMe = getAssignedByMeTasks(channelId, userId);
        List<TaskResponse> ownTasks = Stream.concat(assignedTask.stream(), assignedByMe.stream())
                .filter(distinctById(TaskResponse::getId))
                .sorted(Comparator.comparing(TaskResponse::getCreatedDate).reversed())
                .toList();
        return new TaskReturnService(SUCCESS, "", ownTasks);
    }

    private TaskReturnService validateChannel(String channelId, String userId){
        if (!userRepository.existsById(userId)) {
            return new TaskReturnService(NOT_FOUND, "Not found user", null);
        }

        var channel = channelRepository.findById(channelId).orElse(null);
        if (channel == null) {
            return new TaskReturnService(NOT_FOUND, "Not found channel", null);
        }
        if (!channel.isMember(userId)) {
            return new TaskReturnService(INVALID_PERMISSION, "Not member in channel", null);
        }
        return new TaskReturnService(SUCCESS, "", channel);
    }

    private Predicate<TaskResponse> distinctById(Function<TaskResponse, String> predicater) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(predicater.apply(t));
    }

    private List<TaskResponse> getOwnAssignedTasks(String channelId, String userId) {
        Optional<Group> groupWrapper = groupRepository.findById(channelId);
        if (!groupWrapper.isPresent()) {
            return Collections.emptyList();
        }

        List<Task> tasks =
                taskRepository.findAllByChannelIdAndAssigneeIdsUserIdInOrderByCreatedDateDesc(
                        channelId, Arrays.asList("*", userId));
        return tasks.stream()
                .map(task -> {
                    User assigner = userRepository.findById(task.getAssignerId()).orElse(null);
                    AssigneeDto assignee = task.getAssigneeIds().stream()
                            .filter(a -> a.getUserId().equals(userId))
                            .findFirst()
                            .orElse(null);
                    return TaskResponse.from(task, assigner, assignee, groupWrapper.get());
                })
                .toList();
    }

    public TaskReturnService wrapOwnAssignedTasks(String channelId, String userId) {
        var resultValidate = validateChannel(channelId, userId);
        if (!resultValidate.getReturnCode().equals(SUCCESS)) {
            return resultValidate;
        }

        return new TaskReturnService(SUCCESS, "", getOwnAssignedTasks(channelId, userId));
    }

    private List<TaskResponse> getAssignedByMeTasks(String channelId, String userId) {
        var group = groupRepository.findByChannelIdsContains(channelId);
        if(group == null){
            return Collections.emptyList();
        }

        User assigner = userRepository.findById(userId).orElse(null);
        List<Task> tasks = taskRepository.findAllByChannelIdAndAssignerIdOrderByCreatedDateDesc(channelId, userId);
        return tasks.stream().map(task -> TaskResponse.from(task, assigner, null, group)).toList();
    }

    public TaskReturnService wrapAssignedByMeTasks(String channelId, String userId) {
        var resultValidate = validateChannel(channelId, userId);
        if (!resultValidate.getReturnCode().equals(SUCCESS)) {
            return resultValidate;
        }

        return new TaskReturnService(SUCCESS, "", getAssignedByMeTasks(channelId, userId));
    }

    @Override
    public void saveToReminder(IRemindable remindable) {
        Reminder reminder = remindable.toReminder();
        Task task = (Task) remindable;
        List<String> emailUsers = new ArrayList<>();
        for (AssigneeDto assignee : task.getAssigneeIds()) {
            Optional<User> userOptional = userRepository.findById(assignee.getUserId());
            userOptional.ifPresent(user -> emailUsers.add(user.getEmail()));
        }
        reminder.setRecipients(emailUsers);
        reminder.setSubject("Bạn có 1 công việc sắp tới hạn");
        reminderRepository.save(reminder);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TaskReturnService {
        Integer returnCode;
        String message;
        Object data;

        public TaskReturnService(Integer returnCode, String message, Object data) {
            this.returnCode = returnCode;
            this.message = message;
            this.data = data;
        }
    }
}
