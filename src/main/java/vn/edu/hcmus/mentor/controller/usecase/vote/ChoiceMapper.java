package vn.edu.hcmus.mentor.controller.usecase.vote;

import vn.edu.hcmus.mentor.controller.payload.response.votes.VoteDetailResponse;
import vn.edu.hcmus.mentor.controller.usecase.common.mapper.MapperConverter;
import vn.edu.hcmus.mentor.controller.usecase.vote.common.ChoiceResult;
import vn.edu.hcmus.mentor.domain.Choice;
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