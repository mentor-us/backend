package com.hcmus.mentor.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@Entity
@Table(name = "votes")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel group;

    @Builder.Default
    @OneToMany(mappedBy = "vote", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
   // @Fetch(FetchMode.SUBSELECT)
    private List<Choice> choices = new ArrayList<>();

    public Vote() {

    }

    public Choice getChoice(String id) {
        return choices.stream().filter(choice -> choice.getId().equals(id)).findFirst().orElse(null);
    }

//    public void update(UpdateVoteRequest request) {
//        this.question = request.getQuestion();
//        this.timeEnd = request.getTimeEnd();
//
//        for (var newChoice : request.getChoices()) {
//            Choice oldChoice = getChoice(newChoice.getId());
//            if (oldChoice == null) {
//                choices.add(newChoice);
//            } else {
//                oldChoice.update(newChoice);
//            }
//        }
//    }

//    public void doVoting(DoVotingRequest request) {
//        String voterId = request.getVoterId();
//        choices.forEach(choice -> choice.removeVoting(voterId));
//        request.getChoices().forEach(choice -> {
//            Choice oldChoice = getChoice(choice.getId());
//            if (oldChoice == null) {
//                choices.add(choice);
//                return;
//            }
//            if (choice.getVoters().contains(voterId)) {
//                oldChoice.doVoting(voterId);
//            }
//        });
//        sortChoicesDesc();
//    }

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

}
