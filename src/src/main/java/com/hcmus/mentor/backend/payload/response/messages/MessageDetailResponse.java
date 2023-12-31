package com.hcmus.mentor.backend.payload.response.messages;

import com.hcmus.mentor.backend.entity.*;
import com.hcmus.mentor.backend.payload.FileModel;
import com.hcmus.mentor.backend.payload.response.tasks.TaskMessageResponse;
import com.hcmus.mentor.backend.payload.response.users.ProfileResponse;
import java.util.*;
import java.util.stream.Collectors;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Builder
public class MessageDetailResponse {

  private String id;

  private ProfileResponse sender;

  private String content;

  private Date createdDate;

  private Message.Type type;

  private String groupId;

  private Vote vote;

  private Meeting meeting;

  private TaskMessageResponse task;

  private List<Reaction> reactions;

  private TotalReaction totalReaction;

  private List<Image> images;

  private FileModel file;

  private Message.Status status;

  private ReplyMessage reply;

  public static MessageDetailResponse from(Message message, User user) {
    if (message == null || user == null) {
      return null;
    }

    return MessageDetailResponse.builder()
        .id(message.getId())
        .sender(ProfileResponse.from(user))
        .content(message.getContent())
        .createdDate(message.getCreatedDate())
        .type(message.getType())
        .groupId(message.getGroupId())
        .reactions(message.getReactions())
        .images(transformImageResponse(message.getImages()))
        .file(message.getFile())
        .status(message.getStatus())
        .build();
  }

  private static List<Image> transformImageResponse(List<String> imageUrls) {
    if (imageUrls == null) {
      return Collections.emptyList();
    }
    return imageUrls.stream().map(Image::new).collect(Collectors.toList());
  }

  public static MessageDetailResponse from(MessageResponse message) {
    return MessageDetailResponse.builder()
        .id(message.getId())
        .sender(message.getSender())
        .content(message.getContent())
        .createdDate(message.getCreatedDate())
        .type(message.getType())
        .groupId(message.getGroupId())
        .reactions(message.getReactions())
        .images(transformImageResponse(message.getImages()))
        .file(message.getFile())
        .status(message.getStatus())
        .build();
  }

  public static MessageDetailResponse from(MessageResponse message, Vote vote) {
    return MessageDetailResponse.builder()
        .id(message.getId())
        .sender(message.getSender())
        .content(message.getContent())
        .createdDate(message.getCreatedDate())
        .type(message.getType())
        .groupId(message.getGroupId())
        .vote(vote)
        .reactions(message.getReactions())
        .images(transformImageResponse(message.getImages()))
        .file(message.getFile())
        .status(message.getStatus())
        .build();
  }

  public static MessageDetailResponse from(MessageResponse message, Meeting meeting) {
    return MessageDetailResponse.builder()
        .id(message.getId())
        .sender(message.getSender())
        .content(message.getContent())
        .createdDate(message.getCreatedDate())
        .type(message.getType())
        .groupId(message.getGroupId())
        .meeting(meeting)
        .reactions(message.getReactions())
        .images(transformImageResponse(message.getImages()))
        .file(message.getFile())
        .status(message.getStatus())
        .build();
  }

  public static MessageDetailResponse from(MessageResponse message, TaskMessageResponse task) {
    return MessageDetailResponse.builder()
        .id(message.getId())
        .sender(message.getSender())
        .content(message.getContent())
        .createdDate(message.getCreatedDate())
        .type(message.getType())
        .groupId(message.getGroupId())
        .task(task)
        .reactions(message.getReactions())
        .images(transformImageResponse(message.getImages()))
        .file(message.getFile())
        .status(message.getStatus())
        .build();
  }

  public static MessageDetailResponse normalize(MessageDetailResponse message) {
    ProfileResponse profile = message.getSender();
    if (profile != null
        && profile.getImageUrl() != null
        && ("https://graph.microsoft.com/v1.0/me/photo/$value").equals(profile.getImageUrl())) {
      profile.setImageUrl(null);
    }
    List<Reaction> reactions = message.getReactions();
    if (reactions == null) {
      reactions = new ArrayList<>();
    }
    List<Image> images = message.getImages();
    if (images == null) {
      images = new ArrayList<>();
    }
    return MessageDetailResponse.builder()
        .id(message.getId())
        .sender(profile)
        .content(message.getContent())
        .createdDate(message.getCreatedDate())
        .type(message.getType())
        .groupId(message.getGroupId())
        .vote(message.getVote())
        .meeting(message.getMeeting())
        .task(message.getTask())
        .reactions(reactions)
        .images(images)
        .file(message.getFile())
        .status(message.getStatus())
        .reply(message.getReply())
        .build();
  }

  public static MessageDetailResponse totalReaction(
      MessageDetailResponse message, String viewerId) {
    MessageDetailResponse response = normalize(message);
    TotalReaction totalReaction =
        TotalReaction.builder()
            .data(generateTotalReactionData(message.getReactions()))
            .ownerReacted(generateOwnerReacted(message.getReactions(), viewerId))
            .total(calculateTotalReactionAmount(message.getReactions()))
            .build();
    response.setTotalReaction(totalReaction);
    return response;
  }

  public static List<Emoji> generateTotalReactionData(List<Reaction> reactions) {
    return reactions.stream()
        .flatMap(reaction -> reaction.getData().stream())
        .collect(Collectors.groupingBy(Emoji::getId, Collectors.summingInt(Emoji::getTotal)))
        .entrySet()
        .stream()
        .map(entry -> new Emoji(entry.getKey(), entry.getValue()))
        .sorted(Comparator.comparing(Emoji::getTotal).reversed())
        .collect(Collectors.toList());
  }

  public static List<Emoji> generateOwnerReacted(List<Reaction> reactions, String viewerId) {
    return reactions.stream()
        .filter(reaction -> reaction.getUserId().equals(viewerId))
        .flatMap(reaction -> reaction.getData().stream())
        .sorted(Comparator.comparing(Emoji::getTotal).reversed())
        .collect(Collectors.toList());
  }

  public static Integer calculateTotalReactionAmount(List<Reaction> reactions) {
    return reactions.stream().map(Reaction::getTotal).reduce(0, Integer::sum);
  }

  public boolean isDeletedAttach() {
    if (Message.Type.MEETING.equals(type)) {
      return meeting == null;
    }
    if (Message.Type.TASK.equals(type)) {
      return task == null;
    }
    if (Message.Type.VOTE.equals(type)) {
      return vote == null;
    }
    return false;
  }

  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class Image {

    @Builder.Default private Message.Type type = Message.Type.IMAGE;

    private String url;

    public Image(String url) {
      type = Message.Type.IMAGE;
      this.url = url;
    }
  }

  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class ReplyMessage {
    private String id;
    private String senderName;
    private String content;
  }

  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class TotalReaction {

    @Builder.Default private List<Emoji> data = new ArrayList<>();

    @Builder.Default private List<Emoji> ownerReacted = new ArrayList<>();

    @Builder.Default private int total = 0;
  }
}
