package com.hcmus.mentor.backend.domain;

import com.hcmus.mentor.backend.controller.payload.request.faqs.UpdateFaqRequest;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Builder
@Document("faq")
public class Faq {

    @Id
    private String id;

    private String question;

    private String answer;

    @Builder.Default
    private List<String> voters = new ArrayList<>();

    @Builder.Default
    private Date createdDate = new Date();

    @Builder.Default
    private Date updatedDate = new Date();

    private String creatorId;

    private String groupId;

    @Builder.Default
    private List<String> topicIds = new ArrayList<>();

    public void upvote(String userId) {
        if (voters.contains(userId)) {
            return;
        }
        voters.add(userId);
    }

    public void downVote(String userId) {
        if (!voters.contains(userId)) {
            return;
        }
        voters.remove(userId);
    }

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

    public int getRating() {
        return voters.size();
    }
}
