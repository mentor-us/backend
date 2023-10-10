package com.hcmus.mentor.backend.entity;

import com.hcmus.mentor.backend.payload.request.CreateVoteRequest;
import com.hcmus.mentor.backend.payload.request.DoVotingRequest;
import com.hcmus.mentor.backend.payload.request.UpdateVoteRequest;
import java.util.*;
import java.util.stream.Collectors;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document("voting")
public class Vote {

  @Id private String id;

  private String question;

  @Builder.Default private List<Choice> choices = new ArrayList<>();

  private String groupId;

  private String creatorId;

  private Date timeEnd;

  @Builder.Default private Date createdDate = new Date();

  private Date closedDate;

  @Builder.Default private Status status = Status.OPEN;

  public static Vote from(CreateVoteRequest request) {
    return Vote.builder()
        .question(request.getQuestion())
        .groupId(request.getGroupId())
        .creatorId(request.getCreatorId())
        .timeEnd(request.getTimeEnd())
        .choices(request.getChoices())
        .build();
  }

  public Choice getChoice(String id) {
    return choices.stream().filter(choice -> choice.getId().equals(id)).findFirst().orElse(null);
  }

  public void update(UpdateVoteRequest request) {
    this.question = request.getQuestion();
    this.timeEnd = request.getTimeEnd();

    for (Choice newChoice : request.getChoices()) {
      Choice oldChoice = getChoice(newChoice.getId());
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
              Choice oldChoice = getChoice(choice.getId());
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
    List<Choice> newChoices =
        choices.stream()
            .sorted((c1, c2) -> c2.getVoters().size() - c1.getVoters().size())
            .collect(Collectors.toList());
    setChoices(newChoices);
  }

  public enum Status {
    OPEN,
    CLOSED
  }

  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class Choice {

    @Builder.Default private String id = UUID.randomUUID().toString();

    private String name;

    @Builder.Default private List<String> voters = new ArrayList<>();

    public static Choice from(String name) {
      return Choice.builder().name(name).build();
    }

    public void update(Choice choice) {
      setName(choice.getName());
    }

    public void removeVoting(String voterId) {
      voters.remove(voterId);
    }

    public void doVoting(String voterId) {
      if (voters.contains(voterId)) {
        return;
      }
      voters.add(voterId);
    }
  }
}
