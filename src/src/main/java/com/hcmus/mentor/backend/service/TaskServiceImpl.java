package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.controller.payload.request.AddTaskRequest;
import com.hcmus.mentor.backend.controller.payload.request.UpdateStatusByMentorRequest;
import com.hcmus.mentor.backend.controller.payload.request.UpdateTaskRequest;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageResponse;
import com.hcmus.mentor.backend.controller.payload.response.tasks.*;
import com.hcmus.mentor.backend.controller.payload.response.users.ProfileResponse;
import com.hcmus.mentor.backend.domain.*;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.hcmus.mentor.backend.controller.payload.returnCode.InvalidPermissionCode.INVALID_PERMISSION;
import static com.hcmus.mentor.backend.controller.payload.returnCode.SuccessCode.SUCCESS;
import static com.hcmus.mentor.backend.controller.payload.returnCode.TaskReturnCode.*;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements IRemindableService {

    private final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);
    private final TaskRepository taskRepository;
    private final PermissionService permissionService;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupService groupService;
    private final MessageService messageService;
    private final SocketIOService socketIOService;
    private final ReminderRepository reminderRepository;
    private final NotificationService notificationService;
    private final ChannelRepository channelRepository;

    public TaskReturnService addTask(String loggedUserId, AddTaskRequest request) {
        if (!channelRepository.existsById(request.getGroupId())) {
            logger.error("Add task : Not found channel with id {}", request.getGroupId());
            return new TaskReturnService(NOT_FOUND_GROUP, "Not found channel", null);
        }

        if (request.getTitle() == null || request.getTitle().isEmpty() || request.getDeadline() == null) {
            logger.error("Add task : Not enough required fields : title={}, deadline={}", request.getTitle(), request.getDeadline());
            return new TaskReturnService(NOT_ENOUGH_FIELDS, "Not enough required fields", null);
        }

        var parentTask = taskRepository.findById(request.getParentTask()).orElse(null);
        if (request.getParentTask() != null && parentTask == null && !request.getParentTask().isEmpty()) {
            logger.error("Add task : Not found parent task with id {}", request.getParentTask());
            return new TaskReturnService(NOT_FOUND_PARENT_TASK, "Not found parent task", null);
        }

        if (!request.getUserIds().contains("*")) {
            for (String userId : request.getUserIds()) {
                if (!permissionService.isUserInChannel(request.getGroupId(), userId)) {
                    logger.error("Add task : Not found user with id {} in group {}", userId, request.getGroupId());
                    return new TaskReturnService(NOT_FOUND_USER_IN_GROUP, "Not found user in group", null);
                }
            }
        }

        List<String> userIds = request.getUserIds().contains("*")
                ? groupService.findAllMenteeIdsGroup(request.getGroupId())
                : request.getUserIds();
        User assigner = userRepository.findById(loggedUserId).orElse(null);
        if (assigner == null) {
            logger.error("Add task : Not found user with id {}", loggedUserId);
            return new TaskReturnService(NOT_FOUND, "Not found user", null);
        }
        var assignee = userIds.stream()
                .map(userId -> Assignee.builder().user(userRepository.findById(userId).orElse(null)).build())
                .toList();
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .deadline(request.getDeadline())
                .group(channelRepository.findById(request.getGroupId()).orElse(null))
                .assigner(assigner)
                .parentTask(parentTask)
                .assignees(assignee)
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
                        MessageResponse.from(message, ProfileResponse.from(assigner)));
        socketIOService.sendBroadcastMessage(response, task.getGroup().getId());
        saveToReminder(task);
        notificationService.sendNewTaskNotification(response);

        return new TaskReturnService(SUCCESS, "", task);
    }

    public TaskReturnService getTasksByGroupId(String emailUser, String groupId) {
        if (!groupRepository.existsById(groupId)) {
            logger.error("Get tasks by group id : Not found group with id {}", groupId);
            return new TaskReturnService(NOT_FOUND_GROUP, "Not found group", null);
        }
        if (!permissionService.isInGroup(emailUser, groupId)) {
            logger.error("Get tasks by group id : Invalid permission");
            return new TaskReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }
        List<Task> tasks = taskRepository.findByGroupId(groupId);
        List<TaskDetailResponse> taskDetailResponses = tasks.stream()
                .map(task -> generateTaskDetailFromTask(emailUser, task))
                .sorted(Comparator.comparing(TaskDetailResponse::getCreatedDate).reversed())
                .toList();
        return new TaskReturnService(SUCCESS, "", taskDetailResponses);
    }

    public TaskReturnService getTasksByEmailUser(String emailUser) {
        var user = userRepository.findByEmail(emailUser).orElse(null);
        if (user == null) {
            logger.error("Get tasks by email user : Not found user with email {}", emailUser);
            return new TaskReturnService(NOT_FOUND_USER_IN_GROUP, "Not found user in group", null);
        }
        List<Task> tasks = taskRepository.findByAssigneeIdsUserId(user.getId());
        List<TaskDetailResponse> taskDetailResponses = new ArrayList<>();
        for (Task task : tasks) {
            TaskDetailResponse taskDetailResponse = generateTaskDetailFromTask(emailUser, task);
            taskDetailResponses.add(taskDetailResponse);
        }
        return new TaskReturnService(SUCCESS, "", taskDetailResponses);
    }

    public TaskReturnService deleteTask(CustomerUserDetails user, String id) {
        var task = taskRepository.findById(id).orElse(null);
        if (task == null) {
            logger.error("Delete task : Not found task with id {}", id);
            return new TaskReturnService(NOT_FOUND, "Not found task", null);
        }
        if (!permissionService.isMentor(user.getEmail(), task.getGroup().getId()) && !task.getAssigner().getId().equals(user.getId())) {
            logger.error("Delete task : Invalid permission");
            return new TaskReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }

        taskRepository.delete(task);
        reminderRepository.deleteByRemindableId(task.getId());

        List<Task> childrenTasks = taskRepository.findAllByParentTaskId(id);
        childrenTasks.forEach(
                childrenTask -> {
                    task.setParentTask(null);
                    taskRepository.save(childrenTask);
                });

        return new TaskReturnService(SUCCESS, "", task);
    }

    private TaskDetailResponse generateTaskDetailFromTask(String emailUser, Task task) {
        TaskDetailResponseAssigner assigner = TaskDetailResponseAssigner.from(task.getAssigner());
        TaskDetailResponseGroup groupInfo = TaskDetailResponseGroup.from(task.getGroup().getGroup());
        TaskDetailResponseRole role = permissionService.isMentor(emailUser, task.getGroup().getGroup().getId())
                ? TaskDetailResponseRole.MENTOR
                : TaskDetailResponseRole.MENTEE;

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
            logger.error("Get task : Not found task with id {}", id);
            return new TaskReturnService(NOT_FOUND, "Not found task", null);
        }

        if (!permissionService.isInGroup(emailUser, task.getGroup().getGroup().getId())) {
            logger.error("Get task : Invalid permission");
            return new TaskReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }

        TaskDetailResponse taskDetailResponse = generateTaskDetailFromTask(emailUser, task);
        return new TaskReturnService(SUCCESS, "", taskDetailResponse);
    }

    public TaskReturnService getTaskAssigner(String emailUser, String id) {
        var task = taskRepository.findById(id).orElse(null);
        if (task == null) {
            logger.error("Get task assigner : Not found task with id {}", id);
            return new TaskReturnService(NOT_FOUND, "Not found task", null);
        }

        if (!permissionService.isInGroup(emailUser, task.getGroup().getGroup().getId())) {
            logger.error("Get task assigner : Invalid permission");
            return new TaskReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }

        User user = task.getAssigner();
        ProfileResponse response = ProfileResponse.from(user);
        return new TaskReturnService(SUCCESS, "", response);
    }

    public TaskReturnService getTaskAssigneesWrapper(String emailUser, String id) {
        var task = taskRepository.findById(id).orElse(null);
        if (task == null) {
            logger.error("Get task assignees : Not found task with id {}", id);
            return new TaskReturnService(NOT_FOUND, "Not found task", null);
        }

        if (!permissionService.isInGroup(emailUser, task.getGroup().getGroup().getId())) {
            logger.error("Get task assignees : Invalid permission");
            return new TaskReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }

        return new TaskReturnService(SUCCESS, "", getTaskAssignees(task.getId()));
    }

    public TaskReturnService getTaskAssignees(String taskId) {
        var task = taskRepository.findById(taskId).orElse(null);
        if (task == null) {
            logger.error("Get task assignees : Not found task with id {}", taskId);
            return new TaskReturnService(NOT_FOUND, "Not found task", null);
        }

        Group group = task.getGroup().getGroup();

        var assigneesDto = task.getAssignees().stream().map(Assignee::getUser).toList();

        List<ProfileResponse> assignees = assigneesDto.stream().map(ProfileResponse::from).toList();

        Map<User, TaskStatus> statuses = task.getAssignees().stream()
                .collect(Collectors.toMap(Assignee::getUser, Assignee::getStatus, (s1, s2) -> s2));

        var data = assignees.stream()
                .map(assignee -> {
                    TaskStatus status = statuses.getOrDefault(assignee, null);
                    boolean isMentor = group.isMentor(assignee.getId());
                    return TaskAssigneeResponse.from(assignee, status, isMentor);
                })
                .toList();

        return new TaskReturnService(SUCCESS, "", data);
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
            logger.error("Update status : Not found task with id {}", id);
            return new TaskReturnService(NOT_FOUND, "Not found task", null);
        }

        if (!permissionService.isInGroup(emailUser, task.getGroup().getGroup().getId())) {
            logger.error("Update status : Invalid permission");
            return new TaskReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }

        var user = userRepository.findByEmail(emailUser);
        if (user.isEmpty()) {
            logger.error("Update status : Not found user with email {}", emailUser);
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
            logger.error("Update status by mentor : Not found task with id {}", id);
            return new TaskReturnService(NOT_FOUND, "Not found task", null);
        }

        if (!permissionService.isInGroup(emailUser, task.getGroup().getGroup().getId())) {
            logger.error("Update status by mentor : Invalid permission");
            return new TaskReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }

        var user = userRepository.findByEmail(request.getEmailUserAssigned()).orElse(null);
        if (user == null) {
            logger.error("Update status by mentor : Not found user with email {}", request.getEmailUserAssigned());
            return new TaskReturnService(NOT_FOUND, "Not found user", null);
        }

        if (task.getAssignees().stream().anyMatch(assignee -> assignee.getUser().getId().equals(user.getId()))) {
            logger.error("Update status by mentor : User {} is not assigned in task", request.getEmailUserAssigned());
            return new TaskReturnService(NOT_FOUND_USER_IN_GROUP, "Not found user in group", null);
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
            logger.error("Update task : Not found task with id {}", id);
            return new TaskReturnService(NOT_FOUND, "Not found task", null);
        }

        if (!permissionService.isInGroup(user.getEmail(), task.getGroup().getGroup().getId())) {
            logger.error("Update task : Invalid permission");
            return new TaskReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }
        if (request.getParentTask() != null && !taskRepository.existsById(request.getParentTask())) {
            logger.error("Update task : Not found parent task with id {}", request.getParentTask());
            return new TaskReturnService(NOT_FOUND_PARENT_TASK, "Not found parent task", null);
        }
        for (String assigneeId : request.getUserIds()) {
            if (!permissionService.isUserIdInGroup(assigneeId, task.getGroup().getGroup().getId())) {
                logger.error("Update task : Not found user with id {} in group {}", assigneeId, task.getGroup().getGroup().getId());
                return new TaskReturnService(NOT_FOUND_USER_IN_GROUP, "Not found user in group", null);
            }
        }

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getDeadline() != null) {
            task.setDeadline(request.getDeadline());
        }
        if (request.getUserIds() != null) {
            List<Assignee> assignees = task.getAssignees().stream()
                    .filter(assignee -> request.getUserIds().contains(assignee.getUser().getId()))
                    .toList();

            request.getUserIds().stream()
                    .filter(userId -> assignees.stream().map(Assignee::getUser).noneMatch(u -> u.getId().equals(userId)))
                    .forEach(userId -> assignees.add(Assignee.builder().task(task).user(userRepository.findById(userId).orElse(null)).build()));
            task.setAssignees(assignees);
        }
        if (request.getParentTask() != null) {
            task.setParentTask(taskRepository.findById(request.getParentTask()).orElse(null));
        }
        taskRepository.save(task);

        Reminder reminder = reminderRepository.findByRemindableId(task.getId());
        if (reminder != null) {
            reminder.setReminderDate(task.getReminderDate());
            reminderRepository.save(reminder);
        }

        groupService.pingGroup(task.getGroup().getGroup().getId());

        return new TaskReturnService(SUCCESS, "", task);
    }

    public List<TaskResponse> getMostRecentTasks(String userId) {
        var groups = groupService.getAllActiveOwnGroups(userId);
        List<String> groupIds = groups.stream()
                .map(Group::getChannels)
                .flatMap(Collection::stream)
                .map(Channel::getId)
                .toList();
        List<Task> tasks = taskRepository
                .findTasksByCriteria(userId, groupIds, DateUtils.atStartOfDay(new Date()), PageRequest.of(0, 5, Sort.by(Sort.Order.desc("createdDate"))))
                .getContent();

        return tasks.stream()
                .map(task -> {
                    AssigneeDto assignee = task.getAssignees().stream()
                            .filter(a -> a.getUser().getId().equals(userId))
                            .findFirst()
                            .map(a -> new AssigneeDto(a.getUser().getId(), a.getStatus()))
                            .orElse(null);
                    return TaskResponse.from(task, assignee, task.getGroup().getGroup());
                })
                .toList();
    }

    public List<Task> getAllOwnTasks(String userId) {
        var channels = channelRepository.findOwnChannelsByUserId(userId).stream()
                .toList();

        return channels.stream()
                .map(Channel::getTasks)
                .flatMap(Collection::stream)
                .filter(task -> task.getAssigner().getId().equals(userId) || task.getAssignees().stream().anyMatch(a -> a.getUser().getId().equals(userId)))
                .toList();
    }

    public List<Task> getAllOwnTaskByDate(String userId, Date date) {
        Date startTime = DateUtils.atStartOfDay(date);
        Date endTime = DateUtils.atEndOfDay(date);
        return getAllOwnTasksBetween(userId, startTime, endTime);
    }

    public List<Task> getAllOwnTasksBetween(String userId, Date startTime, Date endTime) {
        var channels = channelRepository.findOwnChannelsByUserId(userId).stream()
                .toList();

        return channels.stream()
                .map(Channel::getTasks)
                .flatMap(Collection::stream)
                .filter(task -> task.getAssigner().getId().equals(userId) || task.getAssignees().stream().anyMatch(a -> a.getUser().getId().equals(userId)))
                .filter(task -> task.getDeadline().after(startTime) && task.getDeadline().before(endTime))
                .toList();
    }

    public List<Task> getAllOwnTasksByMonth(String userId, Date date) {
        Date startTime = DateUtils.atStartOfMonth(date);
        Date endTime = DateUtils.atEndOfMonth(date);
        return getAllOwnTasksBetween(userId, startTime, endTime);
    }

    public TaskServiceImpl.TaskReturnService getAllOwnTasks(String groupId, String userId) {
        if (!userRepository.existsById(userId)) {
            logger.error("Get all own tasks : Not found user with id {}", userId);
            return new TaskReturnService(NOT_FOUND, "Not found user", null);
        }

        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        if (groupWrapper.isEmpty()) {
            logger.error("Get all own tasks : Not found group with id {}", groupId);
            return new TaskReturnService(NOT_FOUND, "Not found group", null);
        }
        Group group = groupWrapper.get();
        if (!group.isMember(userId)) {
            return new TaskReturnService(INVALID_PERMISSION, "Not member in group", null);
        }
//
//        List<TaskResponse> assignedTask = getOwnAssignedTasks(groupId, userId);
//        List<TaskResponse> assignedByMe = getAssignedByMeTasks(groupId, userId);
//        List<TaskResponse> ownTasks = Stream.concat(assignedTask.stream(), assignedByMe.stream())
//                .filter(distinctById(TaskResponse::getId))
//                .sorted(Comparator.comparing(TaskResponse::getCreatedDate).reversed())
//                .toList();
        var abc = group.getChannels().stream()
                .map(Channel::getTasks)
                .flatMap(Collection::stream)
                .filter(task -> task.getAssigner().getId().equals(userId) || task.getAssignees().stream().anyMatch(a -> a.getUser().getId().equals(userId)))
                .map(task -> {
                    AssigneeDto assignee = task.getAssignees().stream()
                            .filter(a -> a.getUser().getId().equals(userId))
                            .findFirst()
                            .map(a -> new AssigneeDto(a.getUser().getId(), a.getStatus()))
                            .orElse(null);
                    return TaskResponse.from(task, assignee, group);
                })
                .sorted(Comparator.comparing(TaskResponse::getCreatedDate).reversed())
                .toList();
        return new TaskReturnService(SUCCESS, "", abc);
    }

//    private Predicate<TaskResponse> distinctById(Function<TaskResponse, String> predicater) {
//        Set<Object> seen = ConcurrentHashMap.newKeySet();
//        return t -> seen.add(predicater.apply(t));
//    }

    private List<TaskResponse> getOwnAssignedTasks(String groupId, String userId) {
        var group = groupRepository.findById(groupId).orElse(null);
        if (group == null) {
            return Collections.emptyList();
        }

        return group.getChannels().stream()
                .map(Channel::getTasks)
                .flatMap(Collection::stream)
                .filter(task -> task.getAssigner().getId().equals(userId) || task.getAssignees().stream().anyMatch(a -> a.getUser().getId().equals(userId)))
                .map(task -> {
                    AssigneeDto assignee = task.getAssignees().stream()
                            .filter(a -> a.getUser().getId().equals(userId))
                            .findFirst()
                            .map(a -> new AssigneeDto(a.getUser().getId(), a.getStatus()))
                            .orElse(null);
                    return TaskResponse.from(task, assignee, group);
                })
                .toList();
    }

    public TaskReturnService wrapOwnAssignedTasks(String groupId, String userId) {
        if (!userRepository.existsById(userId)) {
            logger.error("Wrap own assigned tasks : Not found user with id {}", userId);
            return new TaskReturnService(NOT_FOUND, "Not found user", null);
        }

        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        if (groupWrapper.isEmpty()) {
            logger.error("Wrap own assigned tasks : Not found group with id {}", groupId);
            return new TaskReturnService(NOT_FOUND, "Not found group", null);
        }
        Group group = groupWrapper.get();
        if (!group.isMember(userId)) {
            logger.error("Wrap own assigned tasks : Not member in group");
            return new TaskReturnService(INVALID_PERMISSION, "Not member in group", null);
        }

        return new TaskReturnService(SUCCESS, "", getOwnAssignedTasks(groupId, userId));
    }

    private List<TaskResponse> getAssignedByMeTasks(String groupId, String userId) {
        var group = groupRepository.findById(groupId).orElse(null);
        if (group == null) {
            return Collections.emptyList();
        }
        return group.getChannels().stream()
                .map(Channel::getTasks)
                .flatMap(Collection::stream)
                .filter(task -> task.getAssigner().getId().equals(userId))
                .map(task -> {
                    AssigneeDto assignee = task.getAssignees().stream()
                            .filter(a -> a.getUser().getId().equals(userId))
                            .findFirst()
                            .map(a -> new AssigneeDto(a.getUser().getId(), a.getStatus()))
                            .orElse(null);
                    return TaskResponse.from(task, assignee, group);
                })
                .toList();
    }

    public TaskReturnService wrapAssignedByMeTasks(String groupId, String userId) {
        if (!userRepository.existsById(userId)) {
            logger.error("Wrap assigned by me tasks : Not found user with id {}", userId);
            return new TaskReturnService(NOT_FOUND, "Not found user", null);
        }

        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        if (groupWrapper.isEmpty()) {
            logger.error("Wrap assigned by me tasks : Not found group with id {}", groupId);
            return new TaskReturnService(NOT_FOUND, "Not found group", null);
        }
        Group group = groupWrapper.get();
        if (!group.isMember(userId)) {
            logger.error("Wrap assigned by me tasks : Not member in group");
            return new TaskReturnService(INVALID_PERMISSION, "Not member in group", null);
        }

        return new TaskReturnService(SUCCESS, "", getAssignedByMeTasks(groupId, userId));
    }

    @Override
    public void saveToReminder(IRemindable remindable) {
        Reminder reminder = remindable.toReminder();
        Task task = (Task) remindable;
        reminder.setRecipients(task.getAssignees().stream().map(Assignee::getUser).toList());
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