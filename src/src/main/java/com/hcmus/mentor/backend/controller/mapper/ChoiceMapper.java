package com.hcmus.mentor.backend.controller.mapper;

import com.hcmus.mentor.backend.domain.Choice;
import com.hcmus.mentor.backend.domain.dto.ChoiceDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ChoiceMapper {

    ChoiceMapper INSTANCE = Mappers.getMapper(ChoiceMapper.class);

    @Mapping(source = "voters", target = "voters", qualifiedByName = "getVoterIds")
    ChoiceDto choiceToChoiceDto(Choice choice);

    @Named("getVoterIds")
    static List<String> getVotersId(Choice choice) {
        return choice.getVoters().stream().map(user -> user.getId()).toList();
    }

}
