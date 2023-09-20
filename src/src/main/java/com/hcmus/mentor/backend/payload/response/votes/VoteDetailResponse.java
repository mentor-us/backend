package com.hcmus.mentor.backend.payload.response.votes;

import com.hcmus.mentor.backend.entity.Vote;
import com.hcmus.mentor.backend.payload.response.users.ShortProfile;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VoteDetailResponse {

    @Id
    private String id;

    private String question;

    private List<ChoiceDetail> choices;

    private String groupId;

    private ShortProfile creator;

    private Date timeEnd;

    private Date createdDate;

    private Vote.Status status;

    private Date closedDate;

    private boolean canEdit;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ChoiceDetail {

        private String id;

        private String name;

        private List<ShortProfile> voters;

        public static ChoiceDetail from(Vote.Choice choice, List<ShortProfile> voters) {
            return ChoiceDetail.builder()
                    .id(choice.getId())
                    .name(choice.getName())
                    .voters(voters)
                    .build();
        }
    }

    public static VoteDetailResponse from(Vote vote, ShortProfile creator,
                                          List<ChoiceDetail> choices) {
        return VoteDetailResponse.builder()
                .id(vote.getId())
                .question(vote.getQuestion())
                .groupId(vote.getGroupId())
                .creator(creator)
                .choices(choices)
                .timeEnd(vote.getTimeEnd())
                .createdDate(vote.getCreatedDate())
                .status(vote.getStatus())
                .closedDate(vote.getClosedDate())
                .build();
    }
}
