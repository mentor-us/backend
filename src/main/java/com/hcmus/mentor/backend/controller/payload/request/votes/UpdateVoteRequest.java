package com.hcmus.mentor.backend.controller.payload.request.votes;

import com.hcmus.mentor.backend.domain.dto.ChoiceDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class UpdateVoteRequest {

    private String question;

    private List<ChoiceDto> choices;

    private LocalDateTime timeEnd;
}