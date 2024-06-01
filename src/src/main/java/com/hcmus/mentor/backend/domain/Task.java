package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hcmus.mentor.backend.domain.constant.ReminderType;
import com.hcmus.mentor.backend.domain.method.IRemindable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    private LocalDateTime deadline;

    @Builder.Default
    @Column(name = "created_date", nullable = false)
    private Date createdDate = com.hcmus.mentor.backend.util.DateUtils.getCurrentDateAtUTC();

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Builder.Default
    @Column(name = "deleted_date")
    private Date deletedDate = null;

    @BatchSize(size = 10)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "assigner_id")
    @JsonIgnoreProperties(value = {"messages", "choices", "meetingAttendees", "notificationsSent", "notifications", "notificationSubscribers", "reminders", "faqs", "groupUsers", "channels", "tasksAssigner", "tasksAssignee"}, allowSetters = true)
    private User assigner;

    @Builder.Default
    @ManyToOne
    @JoinColumn(name = "parent_task_id")
    @JsonIgnoreProperties(value = {"assigner", "group", "subTasks", "assignees"}, allowSetters = true)
    private Task parentTask = null;

    @BatchSize(size = 10)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    @JsonIgnoreProperties(value = {"lastMessage", "creator", "group", "tasks", "votes", "meetings", "messagesPinned", "users"}, allowSetters = true)
    private Channel group;

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "parentTask", fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"assigner", "group", "subTasks", "assignees"}, allowSetters = true)
    @ToString.Exclude
    @Fetch(FetchMode.SUBSELECT)
    private List<Task> subTasks = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties(value = {"task", "user"}, allowSetters = true)
    @ToString.Exclude
    @Fetch(FetchMode.SUBSELECT)
    private List<Assignee> assignees = new ArrayList<>();

    @Override
    public Reminder toReminder() {
        var formattedTime = deadline.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        var reminder = Reminder.builder()
                .group(group)
                .name(title)
                .type(ReminderType.TASK)
                .reminderDate(getReminderDate())
                .remindableId(id)
                .build();
        reminder.setProperties("name", title);
        reminder.setProperties("dueDate", formattedTime);
        reminder.setProperties("id", id);

        return reminder;
    }

    public LocalDateTime getReminderDate() {
        return deadline.minusDays(1);
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
}