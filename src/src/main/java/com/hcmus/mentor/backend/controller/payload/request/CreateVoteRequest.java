package com.hcmus.mentor.backend.controller.payload.request;

import com.hcmus.mentor.backend.domain.dto.ChoiceDto;
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
public class CreateVoteRequest {

    private String question;

    private String groupId;

    private String creatorId;

    private Date timeEnd;

    private List<ChoiceDto> choices;

    private Boolean isMultipleChoice = false;
}
