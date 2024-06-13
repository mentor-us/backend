package com.hcmus.mentor.backend.controller.usecase.grade;

import com.hcmus.mentor.backend.controller.usecase.grade.common.GradeDto;
import com.hcmus.mentor.backend.domain.Grade;
import org.modelmapper.ModelMapper;

public class GradeMapper {

    public GradeMapper(ModelMapper modelMapper) {
        modelMapper.createTypeMap(Grade.class, GradeDto.class).addMappings(mapper -> {

        });
    }
}