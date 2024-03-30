package com.hcmus.mentor.backend.controller.payload.response.messages;

import com.hcmus.mentor.backend.controller.payload.File;
import com.hcmus.mentor.backend.controller.payload.response.users.ProfileResponse;
import com.hcmus.mentor.backend.domain.Message;
import com.hcmus.mentor.backend.domain.Reaction;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Builder
public class MessageResponse implements Serializable {

    private String id;

    private ProfileResponse sender;

    private String content;

    private Date createdDate;

    private Message.Type type;

    private String groupId;

    private String voteId;

    private String meetingId;

    private Boolean isEdited;

    private Date editedAt;

    private String taskId;

    private List<Reaction> reactions;

    private List<String> images;

    private File file;

    private Message.Status status;

    private String reply;

    private Boolean isForward = false;

    public static MessageResponse from(Message message, ProfileResponse sender) {
        return MessageResponse.builder()
                .id(message.getId())
                .sender(sender)
                .content(message.getContent())
                .createdDate(message.getCreatedDate())
                .type(message.getType())
                .groupId(message.getGroupId())
                .voteId(message.getVoteId())
                .meetingId(message.getMeetingId())
                .taskId(message.getTaskId())
                .reactions(message.getReactions())
                .images(message.getImages())
                .file(message.getFile())
                .status(message.getStatus())
                .reply(message.getReply())
                .isEdited(message.getIsEdited())
                .editedAt(message.getEditedAt())
                .isForward(message.getIsForward())
                .build();
    }

    @Override
    public String toString() {
        return "Tin nhắn: "
                + "id='"
                + id
                + '\''
                + ", Người gửi="
                + sender.toString()
                + ", Nội dung='"
                + content
                + '\''
                + ", Loại:'"
                + type
                + '\''
                + ", Ngày gửi="
                + createdDate
                + ", Status="
                + status;
    }
}
