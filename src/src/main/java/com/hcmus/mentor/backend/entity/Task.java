package com.hcmus.mentor.backend.entity;

import com.hcmus.mentor.backend.payload.request.UpdateTaskRequest;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document("task")
public class Task implements IRemindable, Serializable {

  @Id private String id;

  private String title;

  private String description;

  private Date deadline;

  private String assignerId;

  @Builder.Default private List<Assignee> assigneeIds = new ArrayList<>();

  @Builder.Default private String parentTask = "";

  private String groupId;

  @Builder.Default private Date createdDate = new Date();

  public static Task.Assignee newTask(String userId) {
    return new Task.Assignee(userId, Task.Status.TO_DO);
  }

  @Override
  public Reminder toReminder() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("name", title);
    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm dd/MM/yyyy");
    String formattedTime = sdf.format(deadline);
    properties.put("dueDate", formattedTime);
    properties.put("id", id);

    return Reminder.builder()
        .groupId(groupId)
        .name(title)
        .type(Reminder.ReminderType.TASK)
        .reminderDate(getReminderDate())
        .properties(properties)
        .remindableId(id)
        .build();
  }

  public Date getReminderDate() {
    LocalDateTime localDateTime =
        LocalDateTime.ofInstant(deadline.toInstant(), ZoneId.systemDefault());
    LocalDateTime reminderTime = localDateTime.minusDays(1);
    return Date.from(reminderTime.atZone(ZoneId.systemDefault()).toInstant());
  }

  @Override
  public String toString() {
    return "Công việc: "
        + "id='"
        + id
        + '\''
        + ", Tiêu đề='"
        + title
        + '\''
        + ", Mô tả='"
        + description
        + '\''
        + ", Ngày tới hạn="
        + deadline
        + ", Ngày tạo="
        + createdDate;
  }

  public List<String> getAllAssigneeIds() {
    return assigneeIds.stream().map(Assignee::getUserId).collect(Collectors.toList());
  }

  public void update(UpdateTaskRequest request) {
    if (request.getTitle() != null) {
      this.setTitle(request.getTitle());
    }
    if (request.getDescription() != null) {
      this.setDescription(request.getDescription());
    }
    if (request.getDeadline() != null) {
      this.setDeadline(request.getDeadline());
    }
    if (request.getUserIds() != null) {
      List<Task.Assignee> assignees =
          assigneeIds.stream()
              .filter(assignee -> request.getUserIds().contains(assignee.getUserId()))
              .collect(Collectors.toList());
      request.getUserIds().stream()
          .filter(userId -> !getAllAssigneeIds().contains(userId))
          .forEach(
              userId -> {
                assignees.add(Assignee.builder().userId(userId).build());
              });
      this.setAssigneeIds(assignees);
    }
    if (request.getParentTask() != null) {
      this.setParentTask(request.getParentTask());
    }
  }

  public enum Status {
    TO_DO,
    IN_PROGRESS,
    DONE,
    OVERDUE
  }

  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class Assignee implements Serializable {
    private String userId;

    @Builder.Default private Status status = Status.TO_DO;
  }
}
