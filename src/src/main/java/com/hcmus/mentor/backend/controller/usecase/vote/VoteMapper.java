package com.hcmus.mentor.backend.controller.usecase.vote;

import com.hcmus.mentor.backend.controller.payload.response.votes.VoteDetailResponse;
import com.hcmus.mentor.backend.controller.usecase.vote.common.VoteResult;
import com.hcmus.mentor.backend.domain.Vote;
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
    }
}