package com.hcmus.mentor.backend.domain;

import com.hcmus.mentor.backend.controller.payload.request.faqs.UpdateFaqRequest;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Builder
@Table(name = "faqs")
public class Faq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    private String question;

    private String answer;

    @Builder.Default
    @OneToMany
    @JoinColumn(name = "voter_id")
    private List<User> voters = new ArrayList<>();

    @Builder.Default
    private Date createdDate = new Date();

    @Builder.Default
    private Date updatedDate = new Date();

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    public Faq() {

    }

//    public void upvote(String userId) {
//        if (voters.contains(userId)) {
//            return;
//        }
//        voters.add(userId);
//    }

//    public void downVote(String userId) {
//        if (!voters.contains(userId)) {
//            return;
//        }
//        voters.remove(userId);
//    }

    public void update(UpdateFaqRequest request) {
        if (request.getQuestion() != null) {
            setQuestion(request.getQuestion());
            setUpdatedDate(new Date());
        }

        if (request.getAnswer() != null) {
            setAnswer(request.getAnswer());
            setUpdatedDate(new Date());
        }
    }

    public void addVoter(User user) {
        if(user == null)
            return;
        voters.add(user);
    }

    public void removeVote(User user) {
        if(user == null)
            return;
        voters.remove(user);
    }

    public int getRating() {
        return voters.size();
    }
}
