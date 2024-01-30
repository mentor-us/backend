package com.hcmus.mentor.backend.service;

import static com.hcmus.mentor.backend.controller.payload.returnCode.InvalidPermissionCode.INVALID_PERMISSION;
import static com.hcmus.mentor.backend.controller.payload.returnCode.SuccessCode.SUCCESS;
import static com.hcmus.mentor.backend.controller.payload.returnCode.TaskReturnCode.*;

import com.hcmus.mentor.backend.domain.*;
import com.hcmus.mentor.backend.domain.method.IRemindable;
import com.hcmus.mentor.backend.controller.payload.request.AddTaskRequest;
import com.hcmus.mentor.backend.controller.payload.request.UpdateStatusByMentorRequest;
import com.hcmus.mentor.backend.controller.payload.request.UpdateTaskRequest;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageResponse;
import com.hcmus.mentor.backend.controller.payload.response.tasks.TaskAssigneeResponse;
import com.hcmus.mentor.backend.controller.payload.response.tasks.TaskDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.tasks.TaskResponse;
import com.hcmus.mentor.backend.controller.payload.response.users.ProfileResponse;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.ReminderRepository;
import com.hcmus.mentor.backend.repository.TaskRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.UserPrincipal;
import com.hcmus.mentor.backend.util.DateUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

@Service
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

    public TaskServiceImpl(
            TaskRepository taskRepository,
            PermissionService permissionService,
            GroupRepository groupRepository,
            UserRepository userRepository,
            MongoTemplate mongoTemplate,
            @Lazy GroupService groupService,
            @Lazy MessageService messageService,
            SocketIOService socketIOService,
            ReminderRepository reminderRepository,
            NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.permissionService = permissionService;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.mongoTemplate = mongoTemplate;
        this.groupService = groupService;
        this.messageService = messageService;
        this.socketIOService = socketIOService;
        this.reminderRepository = reminderRepository;
        this.notificationService = notificationService;
    }

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

        List<String> userIds =
                request.getUserIds().contains("*")
                        ? groupService.findAllMenteeIdsGroup(request.getGroupId())
                        : request.getUserIds();
        Optional<User> assigner = userRepository.findByEmail(emailUser);
        List<Task.Assignee> assigneeIds =
                userIds.stream().map(Task::newTask).collect(Collectors.toList());
        Task task =
                Task.builder()
                        .title(request.getTitle())
                        .description(request.getDescription())
                        .deadline(request.getDeadline())
                        .groupId(request.getGroupId())
                        .assignerId(assigner.map(User::getId).orElse(null))
                        .parentTask(request.getParentTask())
                        .assigneeIds(assigneeIds)
                        .build();
        taskRepository.save(task);

        Message message =
                Message.builder()
                        .senderId(task.getAssignerId())
                        .groupId(task.getGroupId())
                        .createdDate(task.getCreatedDate())
                        .type(Message.Type.TASK)
                        .taskId(task.getId())
                        .build();
        messageService.saveMessage(message);

        MessageDetailResponse response =
                messageService.fulfillTaskMessage(
                        MessageResponse.from(message, ProfileResponse.from(assigner.get())));
        socketIOService.sendBroadcastMessage(response, task.getGroupId());
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
                        .collect(Collectors.toList());
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

    public TaskReturnService deleteTask(UserPrincipal user, String id) {
        Optional<Task> taskOptional = taskRepository.findById(id);
        if (!taskOptional.isPresent()) {
            return new TaskReturnService(NOT_FOUND, "Not found task", null);
        }
        Task task = taskOptional.get();
        if (!permissionService.isMentor(user.getEmail(), task.getGroupId())
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
                        .findById(task.getGroupId())
                        .map(TaskDetailResponse.Group::from)
                        .orElse(null);

        TaskDetailResponse.Role role =
                permissionService.isMentor(emailUser, task.getGroupId())
                        ? TaskDetailResponse.Role.MENTOR
                        : TaskDetailResponse.Role.MENTEE;

        Optional<User> userWrapper = userRepository.findByEmail(emailUser);
        if (!userWrapper.isPresent()) {
            return TaskDetailResponse.from(task, assigner, groupInfo, role, null);
        }

        Task.Status status =
                task.getAssigneeIds().stream()
                        .filter(assignee -> assignee.getUserId().equals(userWrapper.get().getId()))
                        .findFirst()
                        .map(Task.Assignee::getStatus)
                        .orElse(null);
        return TaskDetailResponse.from(task, assigner, groupInfo, role, status);
    }

    public TaskReturnService getTask(String emailUser, String id) {
        Optional<Task> taskOptional = taskRepository.findById(id);
        if (!taskOptional.isPresent()) {
            return new TaskReturnService(NOT_FOUND, "Not found task", null);
        }
        Task task = taskOptional.get();
        if (!permissionService.isInGroup(emailUser, task.getGroupId())) {
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
        if (!permissionService.isInGroup(emailUser, task.getGroupId())) {
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
        if (!permissionService.isInGroup(emailUser, task.getGroupId())) {
            return new TaskReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }

        Optional<Group> groupWrapper = groupRepository.findById(task.getGroupId());
        if (!groupWrapper.isPresent()) {
            return new TaskReturnService(NOT_FOUND, "Not found group", null);
        }
        return new TaskReturnService(SUCCESS, "", getTaskAssignees(task.getId()));
    }

    public List<TaskAssigneeResponse> getTaskAssignees(String taskId) {
        Optional<Task> taskOptional = taskRepository.findById(taskId);
        if (!taskOptional.isPresent()) {
            return Collections.emptyList();
        }

        Task task = taskOptional.get();
        Optional<Group> groupWrapper = groupRepository.findById(task.getGroupId());
        if (!groupWrapper.isPresent()) {
            return Collections.emptyList();
        }
        Group group = groupWrapper.get();
        List<String> assigneeIds =
                task.getAssigneeIds().stream().map(Task.Assignee::getUserId).collect(Collectors.toList());
        List<ProfileResponse> assignees = userRepository.findAllByIdIn(assigneeIds);
        Map<String, Task.Status> statuses =
                task.getAssigneeIds().stream()
                        .collect(
                                Collectors.toMap(
                                        Task.Assignee::getUserId, Task.Assignee::getStatus, (s1, s2) -> s2));
        return assignees.stream()
                .map(
                        assignee -> {
                            Task.Status status = statuses.getOrDefault(assignee.getId(), null);
                            boolean isMentor = group.isMentor(assignee.getId());
                            return TaskAssigneeResponse.from(assignee, status, isMentor);
                        })
                .collect(Collectors.toList());
    }

    private boolean isAssigned(String emailUser, Task task) {
        Optional<User> userOptional = userRepository.findByEmail(emailUser);
        if (!userOptional.isPresent()) {
            return false;
        }
        User user = userOptional.get();
        return taskRepository.existsByIdAndAssigneeIdsUserId(task.getId(), user.getId());
    }

    public TaskReturnService updateStatus(String emailUser, String id, Task.Status status) {
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
        groupService.pingGroup(task.getGroupId());

        return new TaskReturnService(SUCCESS, "", task);
    }

    public TaskReturnService updateStatusByMentor(
            String emailUser, String id, UpdateStatusByMentorRequest request) {
        Optional<Task> taskOptional = taskRepository.findById(id);
        if (!taskOptional.isPresent()) {
            return new TaskReturnService(NOT_FOUND, "Not found task", null);
        }
        Task task = taskOptional.get();
        if (!permissionService.isMentor(emailUser, task.getGroupId())) {
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
        groupService.pingGroup(task.getGroupId());

        return new TaskReturnService(SUCCESS, "", task);
    }

    public TaskReturnService updateTask(UserPrincipal user, String id, UpdateTaskRequest request) {
        Optional<Task> taskOptional = taskRepository.findById(id);
        if (!taskOptional.isPresent()) {
            return new TaskReturnService(NOT_FOUND, "Not found task", null);
        }
        Task task = taskOptional.get();
        if (!permissionService.isMentor(user.getEmail(), task.getGroupId())
                && !task.getAssignerId().equals(user.getId())) {
            return new TaskReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }
        if (request.getParentTask() != null && !taskRepository.existsById(request.getParentTask())) {
            return new TaskReturnService(NOT_FOUND_PARENT_TASK, "Not found parent task", null);
        }
        for (String assigneeId : request.getUserIds()) {
            if (!permissionService.isUserIdInGroup(assigneeId, task.getGroupId())) {
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
        List<String> groupIds =
                groupService.getAllActiveOwnGroups(userId).stream()
                        .map(Group::getId)
                        .collect(Collectors.toList());
        List<Task> tasks =
                taskRepository
                        .findAllByGroupIdInAndAssigneeIdsUserIdInAndDeadlineGreaterThan(
                                groupIds,
                                Arrays.asList("*", userId),
                                new Date(),
                                PageRequest.of(0, 5, Sort.by("deadline").descending()))
                        .getContent();
        return tasks.stream()
                .map(
                        task -> {
                            Group group = groupRepository.findById(task.getGroupId()).orElse(null);
                            User assigner = userRepository.findById(task.getAssignerId()).orElse(null);
                            Task.Assignee assignee =
                                    task.getAssigneeIds().stream()
                                            .filter(a -> a.getUserId().equals(userId))
                                            .findFirst()
                                            .orElse(null);
                            return TaskResponse.from(task, assigner, assignee, group);
                        })
                .collect(Collectors.toList());
    }

    public List<Task> getAllOwnTasks(String userId) {
        List<String> joinedGroupIds =
                groupService.getAllActiveOwnGroups(userId).stream()
                        .map(Group::getId)
                        .collect(Collectors.toList());
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
                        .collect(Collectors.toList());
        MatchOperation match =
                Aggregation.match(
                        Criteria.where("groupId")
                                .in(joinedGroupIds)
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

    public TaskServiceImpl.TaskReturnService getAllOwnTasks(String groupId, String userId) {
        if (!userRepository.existsById(userId)) {
            return new TaskReturnService(NOT_FOUND, "Not found user", null);
        }

        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        if (!groupWrapper.isPresent()) {
            return new TaskReturnService(NOT_FOUND, "Not found group", null);
        }
        Group group = groupWrapper.get();
        if (!group.isMember(userId)) {
            return new TaskReturnService(INVALID_PERMISSION, "Not member in group", null);
        }

        List<TaskResponse> assignedTask = getOwnAssignedTasks(groupId, userId);
        List<TaskResponse> assignedByMe = getAssignedByMeTasks(groupId, userId);
        List<TaskResponse> ownTasks =
                Stream.concat(assignedTask.stream(), assignedByMe.stream())
                        .filter(distinctById(TaskResponse::getId))
                        .sorted(Comparator.comparing(TaskResponse::getCreatedDate).reversed())
                        .collect(Collectors.toList());
        return new TaskReturnService(SUCCESS, "", ownTasks);
    }

    private Predicate<TaskResponse> distinctById(Function<TaskResponse, String> predicater) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(predicater.apply(t));
    }

    private List<TaskResponse> getOwnAssignedTasks(String groupId, String userId) {
        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        if (!groupWrapper.isPresent()) {
            return Collections.emptyList();
        }

        List<Task> tasks =
                taskRepository.findAllByGroupIdAndAssigneeIdsUserIdInOrderByCreatedDateDesc(
                        groupId, Arrays.asList("*", userId));
        return tasks.stream()
                .map(
                        task -> {
                            User assigner = userRepository.findById(task.getAssignerId()).orElse(null);
                            Task.Assignee assignee =
                                    task.getAssigneeIds().stream()
                                            .filter(a -> a.getUserId().equals(userId))
                                            .findFirst()
                                            .orElse(null);
                            return TaskResponse.from(task, assigner, assignee, groupWrapper.get());
                        })
                .collect(Collectors.toList());
    }

    public TaskReturnService wrapOwnAssignedTasks(String groupId, String userId) {
        if (!userRepository.existsById(userId)) {
            return new TaskReturnService(NOT_FOUND, "Not found user", null);
        }

        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        if (!groupWrapper.isPresent()) {
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
        if (!groupWrapper.isPresent()) {
            return Collections.emptyList();
        }
        User assigner = userRepository.findById(userId).orElse(null);
        List<Task> tasks =
                taskRepository.findAllByGroupIdAndAssignerIdOrderByCreatedDateDesc(groupId, userId);
        return tasks.stream()
                .map(task -> TaskResponse.from(task, assigner, null, groupWrapper.get()))
                .collect(Collectors.toList());
    }

    public TaskReturnService wrapAssignedByMeTasks(String groupId, String userId) {
        if (!userRepository.existsById(userId)) {
            return new TaskReturnService(NOT_FOUND, "Not found user", null);
        }

        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        if (!groupWrapper.isPresent()) {
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
        for (Task.Assignee assignee : task.getAssigneeIds()) {
            Optional<User> userOptional = userRepository.findById(assignee.getUserId());
            if (userOptional.isPresent()) {
                emailUsers.add(userOptional.get().getEmail());
            }
        }
        reminder.setRecipients(emailUsers);
        reminder.setSubject("Bạn có 1 công việc sắp tới hạn");
        reminderRepository.save(reminder);
    }

    //    @Override
    //    public List<Reminder> findReminderToday() {
    //        Calendar calendar = Calendar.getInstance();
    //        calendar.setTime(new Date());
    //        calendar.add(Calendar.DAY_OF_MONTH, 1);
    //        Date tomorrow = calendar.getTime();
    //        calendar.add(Calendar.DAY_OF_MONTH, 1);
    //        Date afterTomorrow = calendar.getTime();
    //
    //        List<Task> tasks = taskRepository.findAllByDeadlineBetween(tomorrow, afterTomorrow);
    //        List<Reminder> reminders = new ArrayList<>();
    //        for(Task task: tasks){
    //            Reminder reminder = task.toReminder();
    //            List<String> emailUsers = new ArrayList<>();
    //            for(Task.Assignee assignee: task.getAssigneeIds()){
    //                Optional<User> userOptional = userRepository.findById(assignee.getUserId());
    //                if(userOptional.isPresent()){
    //                    emailUsers.add(userOptional.get().getEmail());
    //                }
    //            }
    //            reminder.setRecipients(emailUsers);
    //            reminders.add(reminder);
    //        }
    //        return reminders;
    //    }

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
