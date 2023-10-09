package com.hcmus.mentor.backend.payload.request;

import com.hcmus.mentor.backend.entity.Vote;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class UpdateVoteRequest {

  private String question;

  private List<Vote.Choice> choices;

  private Date timeEnd;
}
