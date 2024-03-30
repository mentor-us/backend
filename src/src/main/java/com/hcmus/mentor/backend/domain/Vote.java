package com.hcmus.mentor.backend.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Builder
@Data
@Entity
@Table(name = "votes")
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    private String question;

    private Date timeEnd;

    private Date closedDate;

    @Builder.Default
    private Date createdDate = new Date();

    @Builder.Default
    private Boolean isMultipleChoice = false;

    @Builder.Default
    private Status status = Status.OPEN;

    @OneToOne
    @JoinColumn(name = "message_id")
    private Message message;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;

    @ManyToOne
    @JoinColumn(name = "channel_id")
    private Channel group;

    @Builder.Default
    @OneToMany(mappedBy = "vote", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
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
