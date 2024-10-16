package com.hcmus.mentor.backend.controller.usecase.vote;

import com.hcmus.mentor.backend.controller.payload.response.votes.VoteDetailResponse;
import com.hcmus.mentor.backend.controller.usecase.common.mapper.MapperConverter;
import com.hcmus.mentor.backend.controller.usecase.vote.common.ChoiceResult;
import com.hcmus.mentor.backend.domain.Choice;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class ChoiceMapper {

    public ChoiceMapper(ModelMapper modelMapper) {
        modelMapper.createTypeMap(Choice.class, ChoiceResult.class).addMappings(mapping ->
                mapping.using(MapperConverter.mapIdConverter).map(Choice::getVoters, ChoiceResult::setVoters)
        );

        modelMapper.createTypeMap(Choice.class, VoteDetailResponse.ChoiceDetail.class);
    }
}