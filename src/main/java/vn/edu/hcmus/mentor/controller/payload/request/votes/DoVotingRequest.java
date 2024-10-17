package vn.edu.hcmus.mentor.controller.payload.request.votes;

import vn.edu.hcmus.mentor.domain.dto.ChoiceDto;
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

    private List<ChoiceDto> choices;
}