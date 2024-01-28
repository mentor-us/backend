package com.hcmus.mentor.backend.controller.payload.request;

import com.hcmus.mentor.backend.domain.Vote;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class UpdateVoteRequest {

    private String question;

    private List<Vote.Choice> choices;

    private Date timeEnd;
}