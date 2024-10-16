package com.hcmus.mentor.backend.controller.payload.request.messages;

import com.hcmus.mentor.backend.controller.payload.FileModel;
import com.hcmus.mentor.backend.domain.Message;
import com.hcmus.mentor.backend.domain.dto.ReactionDto;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReceivedMessageRequest {
    private String id;

    private String senderId;

    private String content;

    private Date createdDate;

    private Message.Type type = Message.Type.TEXT;

    private String groupId;

    private String voteId;

    private String meetingId;

    private String taskId;

    private Boolean isEdited = true;

    private Date editedAt = null;

    @Builder.Default
    private List<ReactionDto> reactions = new ArrayList<>();

    @Builder.Default
    private List<String> images = new ArrayList<>();

    private FileModel file;

    @Builder.Default
    private Message.Status status = Message.Status.SENT;

    private Message.SystemLog systemLog;

    private String reply;

    @Builder.Default
    private Boolean isForward = false;
}