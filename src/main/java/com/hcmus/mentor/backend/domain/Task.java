package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hcmus.mentor.backend.domain.constant.ReminderType;
import com.hcmus.mentor.backend.domain.method.IRemindable;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
@JsonIgnoreProperties(value = {"assigner", "group", "parentTask", "subTasks", "assignees"}, allowSetters = true)
public class Task implements IRemindable, Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "deadline")
    private Date deadline;

    @Builder.Default
    @Column(name = "created_date", nullable = false)
    private Date createdDate = com.hcmus.mentor.backend.util.DateUtils.getDateNowAtUTC();

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Builder.Default
    @Column(name = "deleted_date")
    private Date deletedDate = null;

    @BatchSize(size = 10)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "assigner_id")
    private User assigner;

    @Builder.Default
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id")
    private Task parentTask = null;

    @BatchSize(size = 10)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel group;

    @Builder.Default
    @OneToMany(mappedBy = "parentTask", fetch = FetchType.LAZY, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private List<Task> subTasks = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private List<Assignee> assignees = new ArrayList<>();

    @Override
    public Reminder toReminder() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm dd/MM/yyyy");
        String formattedTime = sdf.format(DateUtils.addHours(deadline, 7));

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
}