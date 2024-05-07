package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@Entity
@Table(name = "votes")
@NoArgsConstructor
@AllArgsConstructor
public class Vote {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(name = "question")
    private String question;

    @Column(name = "time_end")
    private Date timeEnd;

    @Column(name = "closed_date")
    private Date closedDate;

    @Builder.Default
    @Column(name = "created_date", nullable = false)
    private Date createdDate = new Date();

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Builder.Default
    @Column(name = "deleted_date")
    private Date deletedDate = null;

    @Builder.Default
    @Column(name = "is_multiple_choice", nullable = false)
    private Boolean isMultipleChoice = false;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.OPEN;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    @JsonIgnoreProperties(value = {"messages", "choices", "meetingAttendees", "notificationsSent", "notifications", "notificationSubscribers", "reminders", "faqs", "groupUsers", "channels", "tasksAssigner", "tasksAssignee"}, allowSetters = true)
    private User creator;

    @ManyToOne
    @JoinColumn(name = "channel_id")
    @JsonIgnoreProperties(value = {"lastMessage", "creator", "group", "tasks", "votes", "meetings", "messagesPinned", "users"}, allowSetters = true)
    private Channel group;

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "vote", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = {"creator", "vote", "voters"}, allowSetters = true)
    private List<Choice> choices = new ArrayList<>();

    public Choice getChoice(String id) {
        return choices.stream().filter(choice -> choice.getId().equals(id)).findFirst().orElse(null);
    }

    public void close() {
        setStatus(Status.CLOSED);
        setClosedDate(new Date());
    }

    public void reopen() {
        setStatus(Status.OPEN);
        setClosedDate(null);
    }

    public void sortChoicesDesc() {
        List<Choice> newChoices = choices.stream()
                .sorted((c1, c2) -> c2.getVoters().size() - c1.getVoters().size())
                .toList();
        setChoices(newChoices);
    }

    public enum Status {
        OPEN,
        CLOSED
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "question = " + question + ", " +
                "timeEnd = " + timeEnd + ", " +
                "closedDate = " + closedDate + ", " +
                "createdDate = " + createdDate + ", " +
                "isDeleted = " + isDeleted + ", " +
                "deletedDate = " + deletedDate + ", " +
                "isMultipleChoice = " + isMultipleChoice + ", " +
                "status = " + status + ")";
    }
}