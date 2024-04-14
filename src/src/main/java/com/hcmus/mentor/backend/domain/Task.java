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
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

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
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "deadline")
    private Date deadline;

    @Builder.Default
    @Column(name = "created_date", nullable = false)
    private Date createdDate = new Date();

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Builder.Default
    @Column(name = "deleted_date")
    private Date deletedDate = null;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
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
    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "parentTask", fetch = FetchType.LAZY)
    private List<Task> subTasks = new ArrayList<>();

    @Builder.Default
    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
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
                .group(group)
                .name(title)
                .type(ReminderType.TASK)
                .reminderDate(getReminderDate())
                .propertiesMap(properties)
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

//    public List<User> getAllAssigneeIds() {
//        return assignees.stream().map(Assignee::getUser).toList();
//    }

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
//    }

}
