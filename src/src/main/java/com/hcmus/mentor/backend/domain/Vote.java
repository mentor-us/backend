package com.hcmus.mentor.backend.domain;

import com.hcmus.mentor.backend.controller.payload.request.CreateVoteRequest;
import com.hcmus.mentor.backend.controller.payload.request.DoVotingRequest;
import com.hcmus.mentor.backend.controller.payload.request.UpdateVoteRequest;
import com.hcmus.mentor.backend.domain.dto.ChoiceDto;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document("voting")
public class Vote {

    @Id
    private String id;

    private String question;

    @Builder.Default
    private List<ChoiceDto> choices = new ArrayList<>();

    private String groupId;

    private String creatorId;

    private Date timeEnd;

    @Builder.Default
    private Date createdDate = new Date();

    private Date closedDate;

    @Builder.Default
    private Status status = Status.OPEN;

    public static Vote from(CreateVoteRequest request) {
        return Vote.builder()
                .question(request.getQuestion())
                .groupId(request.getGroupId())
                .creatorId(request.getCreatorId())
                .timeEnd(request.getTimeEnd())
                .choices(request.getChoices())
                .build();
    }

    public ChoiceDto getChoice(String id) {
        return choices.stream().filter(choice -> choice.getId().equals(id)).findFirst().orElse(null);
    }

    public void update(UpdateVoteRequest request) {
        this.question = request.getQuestion();
        this.timeEnd = request.getTimeEnd();

        for (ChoiceDto newChoice : request.getChoices()) {
            ChoiceDto oldChoice = getChoice(newChoice.getId());
            if (oldChoice == null) {
                choices.add(newChoice);
            } else {
                oldChoice.update(newChoice);
            }
        }
    }

    public void doVoting(DoVotingRequest request) {
        String voterId = request.getVoterId();
        choices.forEach(choice -> choice.removeVoting(voterId));
        request
                .getChoices()
                .forEach(
                        choice -> {
                            ChoiceDto oldChoice = getChoice(choice.getId());
                            if (oldChoice == null) {
                                choices.add(choice);
                                return;
                            }
                            if (choice.getVoters().contains(voterId)) {
                                oldChoice.doVoting(voterId);
                            }
                        });
        sortChoicesDesc();
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
        List<ChoiceDto> newChoices =
                choices.stream()
                        .sorted((c1, c2) -> c2.getVoters().size() - c1.getVoters().size())
                        .toList();
        setChoices(newChoices);
    }

    public enum Status {
        OPEN,
        CLOSED
    }

}
