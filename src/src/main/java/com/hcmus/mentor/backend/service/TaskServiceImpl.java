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
import com.hcmus.mentor.backend.domain.constant.TaskStatus;
import com.hcmus.mentor.backend.domain.dto.AssigneeDto;
import com.hcmus.mentor.backend.domain.method.IRemindable;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.ReminderRepository;
import com.hcmus.mentor.backend.repository.TaskRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.userdetails.CustomerUserDetails;
import com.hcmus.mentor.backend.util.DateUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hcmus.mentor.backend.controller.payload.returnCode.InvalidPermissionCode.INVALID_PERMISSION;
import static com.hcmus.mentor.backend.controller.payload.returnCode.SuccessCode.SUCCESS;
import static com.hcmus.mentor.backend.controller.payload.returnCode.TaskReturnCode.*;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements IRemindableService {

    private final TaskRepository taskRepository;
    private final PermissionService permissionService;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupService groupService;
    private final MessageService messageService;
    private final SocketIOService socketIOService;
    private final ReminderRepository reminderRepository;
    private final NotificationService notificationService;

    public TaskReturnService addTask(String emailUser, AddTaskRequest request) {
        if (!groupRepository.existsById(request.getGroupId())) {
            return new TaskReturnService(NOT_FOUND_GROUP, "Not found group", null);
        }
        if (request.getParentTask() != null && !taskRepository.existsById(request.getParentTask())) {
            return new TaskReturnService(NOT_FOUND_PARENT_TASK, "Not found parent task", null);
        }
        if (request.getTitle() == null
                || request.getTitle().isEmpty()
                || request.getDeadline() == null) {
            return new TaskReturnService(NOT_ENOUGH_FIELDS, "Not enough required fields", null);
        }

        if (!request.getUserIds().contains("*")) {
            for (String userId : request.getUserIds()) {
                if (!permissionService.isUserIdInGroup(userId, request.getGroupId())) {
                    return new TaskReturnService(NOT_FOUND_USER_IN_GROUP, "Not found user in group", null);
                }
            }
        }

        List<String> userIds = request.getUserIds().contains("*")
                ? groupService.findAllMenteeIdsGroup(request.getGroupId())
                : request.getUserIds();
        Optional<User> assigner = userRepository.findByEmail(emailUser);
        List<AssigneeDto> assigneeIds = userIds.stream().map(Task::newTask).toList();
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .deadline(request.getDeadline())
                .group(groupRepository.findById(request.getGroupId()).orElse(null))
                .assignerId(assigner.map(User::getId).orElse(null))
                .parentTask(request.getParentTask())
                .assigneeIds(assigneeIds)
                .build();
        taskRepository.save(task);

        Message message = Message.builder()
                .sender(task.getAssigner())
                .channel(task.getGroup())
                .createdDate(task.getCreatedDate())
                .type(Message.Type.TASK)
                .task(task)
                .build();
        messageService.saveMessage(message);

        MessageDetailResponse response =
                messageService.fulfillTaskMessage(
                        MessageResponse.from(message, ProfileResponse.from(assigner.get())));
        socketIOService.sendBroadcastMessage(response, task.getGroup().getId());
        saveToReminder(task);
        notificationService.sendNewTaskNotification(response);

        return new TaskReturnService(SUCCESS, "", task);
    }

    public TaskReturnService getTasksByGroupId(String emailUser, String groupId) {
        if (!groupRepository.existsById(groupId)) {
            return new TaskReturnService(NOT_FOUND_GROUP, "Not found group", null);
        }
        if (!permissionService.isInGroup(emailUser, groupId)) {
            return new TaskReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }
        List<Task> tasks = taskRepository.findByGroupId(groupId);
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
        if (taskOptional.isEmpty()) {
            return new TaskReturnService(NOT_FOUND, "Not found task", null);
        }
        Task task = taskOptional.get();
        if (!permissionService.isMentor(user.getEmail(), task.getGroup().getId())
                && !task.getAssigner().equals(user.getId())) {
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
                        .findById(task.getAssigner())
                        .map(TaskDetailResponse.Assigner::from)
                        .orElse(null);

        TaskDetailResponse.Group groupInfo = TaskDetailResponse.Group.from(task.getGroup().getGroup());

        TaskDetailResponse.Role role =
                permissionService.isMentor(emailUser, task.getGroup().getGroup().getId())
                        ? TaskDetailResponse.Role.MENTOR
                        : TaskDetailResponse.Role.MENTEE;

        var user = userRepository.findByEmail(emailUser).orElse(null);
        if (user == null) {
            return TaskDetailResponse.from(task, assigner, groupInfo, role, null);
        }

        TaskStatus status = task.getAssignees().stream()
                .filter(assignee -> assignee.equals(user))
                .findFirst()
                .map(Assignee::getStatus)
                .orElse(null);
        return TaskDetailResponse.from(task, assigner, groupInfo, role, status);
    }

    public TaskReturnService getTask(String emailUser, String id) {
        var task = taskRepository.findById(id).orElse(null);
        if (task == null) {
            return new TaskReturnService(NOT_FOUND, "Not found task", null);
        }

        if (!permissionService.isInGroup(emailUser, task.getGroup().getGroup().getId())) {
            return new TaskReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }

        TaskDetailResponse taskDetailResponse = generateTaskDetailFromTask(emailUser, task);
        return new TaskReturnService(SUCCESS, "", taskDetailResponse);
    }

    public TaskReturnService getTaskAssigner(String emailUser, String id) {
        var task = taskRepository.findById(id).orElse(null);
        if (task == null) {
            return new TaskReturnService(NOT_FOUND, "Not found task", null);
        }

        if (!permissionService.isInGroup(emailUser, task.getGroup().getGroup().getId())) {
            return new TaskReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }

        User user = task.getAssigner();
        ProfileResponse response = ProfileResponse.from(user);
        return new TaskReturnService(SUCCESS, "", response);
    }

    public TaskReturnService getTaskAssigneesWrapper(String emailUser, String id) {
        var task = taskRepository.findById(id).orElse(null);
        if (task == null) {
            return new TaskReturnService(NOT_FOUND, "Not found task", null);
        }

        if (!permissionService.isInGroup(emailUser, task.getGroup().getGroup().getId())) {
            return new TaskReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }

        return new TaskReturnService(SUCCESS, "", getTaskAssignees(task.getId()));
    }

    public List<TaskAssigneeResponse> getTaskAssignees(String taskId) {
        var task = taskRepository.findById(taskId).orElse(null);
        if (task == null) {
            return Collections.emptyList();
        }
//
//        Optional<Group> groupOpt = groupRepository.findById(task.getGroupId());
//        if (groupOpt.isEmpty()) {
//            return Collections.emptyList();
//        }
//        Group group = groupOpt.get();
        Group group = task.getGroup().getGroup();

        var assigneesDto = task.getAssignees().stream()
                .map(Assignee::getUser).toList();

        List<ProfileResponse> assignees = assigneesDto.stream()
                .map(ProfileResponse::from)
                .toList();

        Map<User, TaskStatus> statuses = task.getAssignees().stream()
                .collect(Collectors.toMap(Assignee::getUser, Assignee::getStatus, (s1, s2) -> s2));

        return assignees.stream()
                .map(assignee -> {
                    TaskStatus status = statuses.getOrDefault(assignee, null);
                    boolean isMentor = group.isMentor(assignee.getId());
                    return TaskAssigneeResponse.from(assignee, status, isMentor);
                })
                .toList();
    }

    private boolean isAssigned(String emailUser, Task task) {
        Optional<User> userOptional = userRepository.findByEmail(emailUser);
        if (userOptional.isEmpty()) {
            return false;
        }
        User user = userOptional.get();
        return taskRepository.existsByIdAndAssigneeIdsUserId(task.getId(), user.getId());
    }

    public TaskReturnService updateStatus(String emailUser, String id, TaskStatus status) {
        var task = taskRepository.findById(id).orElse(null);
        if (task == null) {
            return new TaskReturnService(NOT_FOUND, "Not found task", null);
        }

        if (!permissionService.isInGroup(emailUser, task.getGroup().getGroup().getId())) {
            return new TaskReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }

        var user = userRepository.findByEmail(emailUser);
        if (user.isEmpty()) {
            return new TaskReturnService(NOT_FOUND, "Not found user", null);
        }

        task.getAssignees().stream()
                .filter(assignee -> assignee.getUser().equals(user))
                .forEach(assignee -> assignee.setStatus(status));
        taskRepository.save(task);
        groupService.pingGroup(task.getGroup().getId());

        return new TaskReturnService(SUCCESS, "", task);
    }

    public TaskReturnService updateStatusByMentor(String emailUser, String id, UpdateStatusByMentorRequest request) {
        var task = taskRepository.findById(id).orElse(null);
        if (task == null) {
            return new TaskReturnService(NOT_FOUND, "Not found task", null);
        }

        if (!permissionService.isInGroup(emailUser, task.getGroup().getGroup().getId())) {
            return new TaskReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }

        if (!isAssigned(request.getEmailUserAssigned(), task)) {
            return new TaskReturnService(NOT_FOUND, "Not found task", null);
        }
        var user = userRepository.findByEmail(request.getEmailUserAssigned());
        if (user.isEmpty()) {
            return new TaskReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }

        task.getAssignees().stream()
                .filter(assignee -> assignee.getUser().equals(user))
                .forEach(assignee -> assignee.setStatus(request.getStatus()));
        taskRepository.save(task);
        groupService.pingGroup(task.getGroup().getId());

        return new TaskReturnService(SUCCESS, "", task);
    }

    public TaskReturnService updateTask(CustomerUserDetails user, String id, UpdateTaskRequest request) {
        var task = taskRepository.findById(id).orElse(null);
        if (task == null) {
            return new TaskReturnService(NOT_FOUND, "Not found task", null);
        }

        if (!permissionService.isInGroup(user.getEmail(), task.getGroup().getGroup().getId())) {
            return new TaskReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }
        if (request.getParentTask() != null && !taskRepository.existsById(request.getParentTask())) {
            return new TaskReturnService(NOT_FOUND_PARENT_TASK, "Not found parent task", null);
        }
        for (String assigneeId : request.getUserIds()) {
            if (!permissionService.isUserIdInGroup(assigneeId, task.getGroup().)) {
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

        groupService.pingGroup(task.getGroupId());

        return new TaskReturnService(SUCCESS, "", task);
    }

    public List<TaskResponse> getMostRecentTasks(String userId) {
        List<String> groupIds = groupService.getAllActiveOwnGroups(userId).stream()
                .map(Group::getId)
                .toList();
        List<Task> tasks =
                taskRepository
                        .findAllByGroupIdInAndAssigneeIdsUserIdInAndDeadlineGreaterThan(
                                groupIds,
                                Arrays.asList("*", userId),
                                new Date(),
                                PageRequest.of(0, 5, Sort.by("deadline").descending()))
                        .getContent();
        return tasks.stream()
                .map(task -> {
                    Group group = groupRepository.findById(task.getGroupId()).orElse(null);
                    User assigner = userRepository.findById(task.getAssigner()).orElse(null);
                    AssigneeDto assignee = task.getAssignees().stream()
                            .filter(a -> a.getUserId().equals(userId))
                            .findFirst()
                            .orElse(null);
                    return TaskResponse.from(task, assigner, assignee, group);
                })
                .toList();
    }

    public List<Task> getAllOwnTasks(String userId) {
        List<String> joinedGroupIds = groupService.getAllActiveOwnGroups(userId).stream()
                .map(Group::getId)
                .toList();

        return taskRepository.findAllByGroupIdInAndAssigneeIdsUserIdIn(
                joinedGroupIds, Arrays.asList("*", userId));
    }

    public List<Task> getAllOwnTaskByDate(String userId, Date date) {
        Date startTime = DateUtils.atStartOfDay(date);
        Date endTime = DateUtils.atEndOfDay(date);
        return getAllOwnTasksBetween(userId, startTime, endTime);
    }

    public List<Task> getAllOwnTasksBetween(String userId, Date startTime, Date endTime) {
        List<String> joinedGroupIds =
                groupService.getAllActiveOwnGroups(userId).stream()
                        .map(Group::getId)
                        .toList();
//        MatchOperation match = Aggregation.match(
//                Criteria.where("groupId")
//                        .in(joinedGroupIds)
//                        .and("assigneeIds.userId")
//                        .in(Arrays.asList("*", userId))
//                        .and("deadline")
//                        .gte(startTime)
//                        .lte(endTime));
//        Aggregation aggregation = Aggregation.newAggregation(match);
//        return mongoTemplate.aggregate(aggregation, "task", Task.class).getMappedResults();
        // TODO: Make sure this is correct

        return taskRepository.findByGroupIdInAndAssigneeIdsContainingAndDeadlineBetween(
                joinedGroupIds, userId, startTime, endTime);
    }

    public List<Task> getAllOwnTasksByMonth(String userId, Date date) {
        Date startTime = DateUtils.atStartOfMonth(date);
        Date endTime = DateUtils.atEndOfMonth(date);
        return getAllOwnTasksBetween(userId, startTime, endTime);
    }

    public TaskServiceImpl.TaskReturnService getAllOwnTasks(String groupId, String userId) {
        if (!userRepository.existsById(userId)) {
            return new TaskReturnService(NOT_FOUND, "Not found user", null);
        }

        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        if (groupWrapper.isEmpty()) {
            return new TaskReturnService(NOT_FOUND, "Not found group", null);
        }
        Group group = groupWrapper.get();
        if (!group.isMember(userId)) {
            return new TaskReturnService(INVALID_PERMISSION, "Not member in group", null);
        }

        List<TaskResponse> assignedTask = getOwnAssignedTasks(groupId, userId);
        List<TaskResponse> assignedByMe = getAssignedByMeTasks(groupId, userId);
        List<TaskResponse> ownTasks = Stream.concat(assignedTask.stream(), assignedByMe.stream())
                .filter(distinctById(TaskResponse::getId))
                .sorted(Comparator.comparing(TaskResponse::getCreatedDate).reversed())
                .toList();
        return new TaskReturnService(SUCCESS, "", ownTasks);
    }

    private Predicate<TaskResponse> distinctById(Function<TaskResponse, String> predicater) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(predicater.apply(t));
    }

    private List<TaskResponse> getOwnAssignedTasks(String groupId, String userId) {
        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        if (groupWrapper.isEmpty()) {
            return Collections.emptyList();
        }

        List<Task> tasks =
                taskRepository.findAllByGroupIdAndAssigneeIdsUserIdInOrderByCreatedDateDesc(
                        groupId, Arrays.asList("*", userId));
        return tasks.stream()
                .map(task -> {
                    User assigner = userRepository.findById(task.getAssigner()).orElse(null);
                    AssigneeDto assignee = task.getAssignees().stream()
                            .filter(a -> a.getUserId().equals(userId))
                            .findFirst()
                            .orElse(null);
                    return TaskResponse.from(task, assigner, assignee, groupWrapper.get());
                })
                .toList();
    }

    public TaskReturnService wrapOwnAssignedTasks(String groupId, String userId) {
        if (!userRepository.existsById(userId)) {
            return new TaskReturnService(NOT_FOUND, "Not found user", null);
        }

        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        if (groupWrapper.isEmpty()) {
            return new TaskReturnService(NOT_FOUND, "Not found group", null);
        }
        Group group = groupWrapper.get();
        if (!group.isMember(userId)) {
            return new TaskReturnService(INVALID_PERMISSION, "Not member in group", null);
        }

        return new TaskReturnService(SUCCESS, "", getOwnAssignedTasks(groupId, userId));
    }

    private List<TaskResponse> getAssignedByMeTasks(String groupId, String userId) {
        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        if (groupWrapper.isEmpty()) {
            return Collections.emptyList();
        }
        User assigner = userRepository.findById(userId).orElse(null);
        List<Task> tasks =
                taskRepository.findAllByGroupIdAndAssignerIdOrderByCreatedDateDesc(groupId, userId);
        return tasks.stream()
                .map(task -> TaskResponse.from(task, assigner, null, groupWrapper.get()))
                .toList();
    }

    public TaskReturnService wrapAssignedByMeTasks(String groupId, String userId) {
        if (!userRepository.existsById(userId)) {
            return new TaskReturnService(NOT_FOUND, "Not found user", null);
        }

        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        if (groupWrapper.isEmpty()) {
            return new TaskReturnService(NOT_FOUND, "Not found group", null);
        }
        Group group = groupWrapper.get();
        if (!group.isMember(userId)) {
            return new TaskReturnService(INVALID_PERMISSION, "Not member in group", null);
        }

        return new TaskReturnService(SUCCESS, "", getAssignedByMeTasks(groupId, userId));
    }

    @Override
    public void saveToReminder(IRemindable remindable) {
        Reminder reminder = remindable.toReminder();
        Task task = (Task) remindable;
        List<String> emailUsers = new ArrayList<>();
        for (AssigneeDto assignee : task.getAssignees()) {
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
