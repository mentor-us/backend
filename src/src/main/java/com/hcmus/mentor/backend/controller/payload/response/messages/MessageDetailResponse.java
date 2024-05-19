package com.hcmus.mentor.backend.controller.payload.response.messages;

import com.hcmus.mentor.backend.controller.payload.FileModel;
import com.hcmus.mentor.backend.controller.payload.response.tasks.TaskMessageResponse;
import com.hcmus.mentor.backend.controller.payload.response.users.ProfileResponse;
import com.hcmus.mentor.backend.controller.usecase.vote.common.VoteResult;
import com.hcmus.mentor.backend.domain.*;
import com.hcmus.mentor.backend.domain.constant.EmojiType;
import com.hcmus.mentor.backend.domain.dto.EmojiDto;
import com.hcmus.mentor.backend.domain.dto.ReactionDto;
import com.hcmus.mentor.backend.service.dto.MeetingDto;
import lombok.*;

import java.io.Serializable;
import java.util.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Builder
public class MessageDetailResponse implements Serializable {

    private String id;

    private ProfileResponse sender;

    private String content;

    private Date createdDate;

    private Message.Type type;

    private Boolean isEdited;

    private Date editedAt;

    private String groupId;

    private VoteResult vote;

    private MeetingDto meeting;

    private TaskMessageResponse task;

    private List<ReactionDto> reactions;

    private TotalReaction totalReaction;

    private List<Image> images;

    private FileModel file;

    private Message.Status status;

    private ReplyMessage reply;

    @Builder.Default
    private Boolean isForward = false;


    private static List<Image> transformImageResponse(List<String> imageUrls) {
        if (imageUrls == null) {
            return Collections.emptyList();
        }
        return imageUrls.stream().map(Image::new).toList();
    }

    public static List<EmojiDto> generateTotalReactionData(List<ReactionDto> reactions) {
        return Arrays.stream(EmojiType.values())
                .map(et -> new EmojiDto(EmojiType.valueOf(
                        et.name()),
                        reactions.stream()
                                .map(ReactionDto::getData)
                                .flatMap(Collection::stream)
                                .filter(r -> r.getId().equals(et)).map(EmojiDto::getTotal)
                                .reduce(0, Integer::sum)))
                .sorted(Comparator.comparing(EmojiDto::getTotal).reversed()).toList();
    }

    public static Integer calculateTotalReactionAmount(List<ReactionDto> reactions) {
        return reactions.stream().map(ReactionDto::getTotal).reduce(0, Integer::sum);
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

        @Builder.Default
        private Message.Type type = Message.Type.IMAGE;

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

        @Builder.Default
        private List<EmojiDto> data = new ArrayList<>();

        @Builder.Default
        private List<EmojiDto> ownerReacted = new ArrayList<>();

        @Builder.Default
        private int total = 0;
    }
}