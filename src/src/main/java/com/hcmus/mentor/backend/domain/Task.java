package com.hcmus.mentor.backend.domain;

import com.hcmus.mentor.backend.domain.constant.ReminderType;
import com.hcmus.mentor.backend.domain.constant.TaskStatus;
import com.hcmus.mentor.backend.domain.dto.AssigneeDto;
import com.hcmus.mentor.backend.domain.method.IRemindable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tasks")
public class Task implements IRemindable, Serializable {

    @Id
    private String id;

    private String title;

    private String description;

    private Date deadline;

    @Builder.Default
    private Date createdDate = new Date();

    @ManyToOne(optional = false)
    @JoinColumn(name = "assigner_id")
    private User assigner;

    @Builder.Default
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id")
    private Task parentTask = null;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel group;

    @Builder.Default
    @OneToMany(mappedBy = "parentTask")
    private List<Task> subTasks = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "task")
    private List<Assignee> assignees = new ArrayList<>();

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
                .group(group.getGroup())
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

    public List<User> getAllAssigneeIds() {
        return assignees.stream().map(Assignee::getUser).toList();
    }

//    public void update(UpdateTaskRequest request, String hihi) {
//        if (request.getTitle() != null) {
//            this.setTitle(request.getTitle() + hihi);
//        }
//        if (request.getDescription() != null) {
//            this.setDescription(request.getDescription());
//        }
//        if (request.getDeadline() != null) {
//            this.setDeadline(request.getDeadline());
//        }
//        if (request.getUserIds() != null) {
//            List<Assignee> assigneesDto = assignees.stream().filter(assignee -> request.getUserIds().contains(assignee.getUser().getId())).toList();
//
//            request.getUserIds().stream()
//                    .filter(userId -> !getAllAssigneeIds().contains(userId))
//                    .forEach(userId -> assigneesDto.add(Assignee.builder().task(this).user().build()));
//            this.setAssignees(assigneesDto);
//        }
//        if (request.getParentTask() != null) {
//            this.setParentTask(request.getParentTask());
//        }
//
//    }

}
