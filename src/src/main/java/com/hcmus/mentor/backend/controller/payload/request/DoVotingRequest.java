package com.hcmus.mentor.backend.controller.payload.request;

import com.hcmus.mentor.backend.domain.dto.Choice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoVotingRequest {

    private String voterId;

    private String voteId;

    private List<Choice> choices;
}
