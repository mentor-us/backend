package vn.edu.hcmus.mentor.controller.payload.request.votes;

import vn.edu.hcmus.mentor.domain.dto.ChoiceDto;
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
public class CreateVoteRequest {

    private String question;

    private String groupId;

    private String creatorId;

    private LocalDateTime timeEnd;

    private List<ChoiceDto> choices;

    private Boolean isMultipleChoice = false;
}