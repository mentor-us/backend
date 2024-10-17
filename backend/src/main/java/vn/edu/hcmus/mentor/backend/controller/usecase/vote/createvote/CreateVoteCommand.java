package vn.edu.hcmus.mentor.backend.controller.usecase.vote.createvote;

import an.awesome.pipelinr.Command;
import vn.edu.hcmus.mentor.backend.controller.usecase.vote.common.VoteResult;
import vn.edu.hcmus.mentor.backend.domain.dto.ChoiceDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVoteCommand implements Command<VoteResult> {

    private String question;
    private String groupId;
    private String creatorId;
    private LocalDateTime timeEnd;
    private List<ChoiceDto> choices;
    private Boolean isMultipleChoice = false;
}
