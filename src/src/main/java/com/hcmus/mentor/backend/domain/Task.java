package com.hcmus.mentor.backend.domain;

import com.hcmus.mentor.backend.controller.payload.request.UpdateTaskRequest;
import com.hcmus.mentor.backend.domain.constant.ReminderType;
import com.hcmus.mentor.backend.domain.constant.TaskStatus;
import com.hcmus.mentor.backend.domain.dto.AssigneeDto;
import com.hcmus.mentor.backend.domain.method.IRemindable;
import lombok.*;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document("task")
public class Task implements IRemindable, Serializable {

    @Id
    private String id;

    private String title;

    private String description;

    private Date deadline;

    private String assignerId;

    @Builder.Default
    private List<AssigneeDto> assigneeIds = new ArrayList<>();

    @Builder.Default
    private String parentTask = "";

    private String groupId;

    @Builder.Default
    private Date createdDate = new Date();

    public static AssigneeDto newTask(String userId) {
        return new AssigneeDto(userId, TaskStatus.TO_DO);
    }

    @Override
    public Reminder toReminder() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm dd/MM/yyyy");
        String formattedTime = sdf.format(DateUtils.addHours(deadline, 7));

        Map<String, Object> properties = new HashMap<>();

        properties.put("name", title);
        properties.put("dueDate", formattedTime);
        properties.put("id", id);

        return Reminder.builder()
                .groupId(groupId)
                .name(title)
                .type(ReminderType.TASK)
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
        return assigneeIds.stream().map(AssigneeDto::getUserId).collect(Collectors.toList());
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
            List<AssigneeDto> assignees =
                    assigneeIds.stream()
                            .filter(assignee -> request.getUserIds().contains(assignee.getUserId()))
                            .collect(Collectors.toList());
            request.getUserIds().stream()
                    .filter(userId -> !getAllAssigneeIds().contains(userId))
                    .forEach(
                            userId -> {
                                assignees.add(AssigneeDto.builder().userId(userId).build());
                            });
            this.setAssigneeIds(assignees);
        }
        if (request.getParentTask() != null) {
            this.setParentTask(request.getParentTask());
        }
    }

}
