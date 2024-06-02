package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hcmus.mentor.backend.util.DateUtils;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Builder
@Table(name = "faqs")
@AllArgsConstructor
public class Faq implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "question", nullable = false)
    private String question;

    @Column(name = "answer")
    private String answer;

    @Builder.Default
    @Column(name = "created_date", nullable = false)
    private Date createdDate = DateUtils.getCurrentDateAtUTC() ;

    @Builder.Default
    @Column(name = "updated_date", nullable = false)
    private Date updatedDate = DateUtils.getCurrentDateAtUTC() ;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    @JsonIgnoreProperties(value = {"messages", "choices", "meetingAttendees", "notificationsSent", "notifications", "notificationSubscribers", "reminders", "faqs", "groupUsers", "channels", "tasksAssigner", "tasksAssignee"}, allowSetters = true)
    private User creator;

    @ManyToOne
    @JoinColumn(name = "group_id")
    @JsonIgnoreProperties(value = {"lastMessage", "defaultChannel", "channels", "groupCategory", "creator", "channels", "faqs", "groupUsers"}, allowSetters = true)
    private Group group;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "rel_faq_user_voter", joinColumns = @JoinColumn(name = "faq_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    @JsonIgnoreProperties(value = {"messages", "choices", "meetingAttendees", "notificationsSent", "notifications", "notificationSubscribers", "reminders", "faqs", "groupUsers", "channels", "tasksAssigner", "tasksAssignee"}, allowSetters = true)
    private List<User> voters = new ArrayList<>();

    public Faq() {
    }


    public int getRating() {
        return voters.size();
    }
}