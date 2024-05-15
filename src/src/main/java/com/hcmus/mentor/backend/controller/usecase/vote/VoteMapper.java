package com.hcmus.mentor.backend.controller.usecase.vote;

import com.hcmus.mentor.backend.controller.payload.response.votes.VoteDetailResponse;
import com.hcmus.mentor.backend.controller.usecase.vote.common.VoteResult;
import com.hcmus.mentor.backend.controller.usecase.vote.createvote.CreateVoteCommand;
import com.hcmus.mentor.backend.domain.Choice;
import com.hcmus.mentor.backend.domain.Vote;
import com.hcmus.mentor.backend.domain.dto.ChoiceDto;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class VoteMapper {

    public VoteMapper(ModelMapper modelMapper) {
        modelMapper.createTypeMap(Vote.class, VoteResult.class).addMappings(mapping -> {
            mapping.map(src -> src.getGroup().getId(), VoteResult::setGroupId);
            mapping.map(src -> src.getCreator().getId(), VoteResult::setCreatorId);
        });
        modelMapper.createTypeMap(Vote.class, VoteDetailResponse.class).addMappings(mapping ->
                mapping.map(src -> src.getGroup().getId(), VoteDetailResponse::setGroupId)
        );
        modelMapper.createTypeMap(CreateVoteCommand.class, Vote.class).addMappings(mapping -> {
            mapping.map(CreateVoteCommand::getCreatorId, Vote::setId); // For not have error matches multiple source.
            mapping.skip(Vote::setId);
            mapping.skip(Vote::setGroup);
            mapping.skip(Vote::setChoices);
            mapping.skip(Vote::setCreator);
        });
        modelMapper.createTypeMap(ChoiceDto.class, Choice.class).addMappings(mapping -> {
            mapping.skip(Choice::setVote);
            mapping.skip(Choice::setCreator);
            mapping.skip(Choice::setVoters);
        });
    }
}