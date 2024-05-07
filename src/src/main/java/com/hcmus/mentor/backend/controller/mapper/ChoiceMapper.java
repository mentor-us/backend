package com.hcmus.mentor.backend.controller.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ChoiceMapper {

    ChoiceMapper INSTANCE = Mappers.getMapper(ChoiceMapper.class);

//    @Mapping(source = "voters", target = "voters", qualifiedByName = "getVoterIds")
//    ChoiceDto choiceToChoiceDto(Choice choice);
//
//    @Named("getVoterIds")
//    static List<String> getVotersId(Choice choice) {
//        return choice.getVoters().stream().map(user -> user.getId()).toList();
//    }

}
