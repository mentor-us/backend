package com.hcmus.mentor.backend.controller.usecase.vote.common;

import com.hcmus.mentor.backend.domain.Vote;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class VoteResult {

    private String id;

    private String question;

    @Builder.Default
    private List<ChoiceResult> choices = new ArrayList<>();

    private String groupId;

    private String creatorId;

    private LocalDateTime timeEnd;

    private LocalDateTime creationDate;

    private LocalDateTime closedDate;

    private Boolean isMultipleChoice;

    @Builder.Default
    private Vote.Status status = Vote.Status.OPEN;
}