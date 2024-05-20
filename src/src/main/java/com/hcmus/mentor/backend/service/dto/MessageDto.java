package com.hcmus.mentor.backend.service.dto;

import com.hcmus.mentor.backend.controller.payload.FileModel;
import com.hcmus.mentor.backend.domain.Message;
import com.hcmus.mentor.backend.domain.dto.ReactionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {
    private String id;

    private String senderId;

    private String content;

    private Date createdDate;

    private Message.Type type;

    private String groupId;

    private String voteId;

    private String meetingId;

    private String taskId;

    private Boolean isEdited;

    private Date editedAt;

    @Builder.Default
    private List<ReactionDto> reactions = new ArrayList<>();

    @Builder.Default
    private List<String> images = new ArrayList<>();

    private FileModel file;

    @Builder.Default
    private Message.Status status = Message.Status.SENT;

    private String reply;

    @Builder.Default
    private Boolean isForward = false;

    public static MessageDto from(Message message) {
        return MessageDto.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .content(message.getContent())
                .createdDate(message.getCreatedDate())
                .type(message.getType())
                .groupId(message.getChannel().getId())
                .voteId(message.getVote().getId())
                .meetingId(message.getMeeting() != null ? message.getMeeting().getId() : null)
                .taskId(message.getTask() != null ? message.getTask().getId() : null)
                .editedAt(message.getEditedAt())
//                .reactions(ReactionDto.from(message.getReactions()))
                .images(message.getImages())
                .file(new FileModel(message.getFile()))
                .status(message.getStatus())
                .reply(message.getReply())
                .isForward(message.getIsForward())
                .build();
    }
}