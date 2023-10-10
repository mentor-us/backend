package com.hcmus.mentor.backend.payload.request;

import com.hcmus.mentor.backend.entity.Vote;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoVotingRequest {

  private String voterId;

  private String voteId;

  private List<Vote.Choice> choices;
}
