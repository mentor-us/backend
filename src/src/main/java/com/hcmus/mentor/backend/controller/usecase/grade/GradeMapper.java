package com.hcmus.mentor.backend.controller.usecase.grade;

import com.hcmus.mentor.backend.controller.usecase.common.mapper.MapperConverter;
import com.hcmus.mentor.backend.controller.usecase.grade.common.GradeDto;
import com.hcmus.mentor.backend.controller.usecase.grade.common.GradeVersionDto;
import com.hcmus.mentor.backend.domain.Grade;
import com.hcmus.mentor.backend.domain.GradeHistory;
import com.hcmus.mentor.backend.domain.GradeVersion;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class GradeMapper {

    public GradeMapper(ModelMapper modelMapper) {
        modelMapper.createTypeMap(Grade.class, GradeDto.class).addMappings(mapper -> {
            mapper.using(MapperConverter.toLocalDateTime).map(Grade::getCreatedDate, GradeDto::setCreatedDate);
            mapper.using(MapperConverter.toLocalDateTime).map(Grade::getUpdatedDate, GradeDto::setUpdatedDate);
        });

        modelMapper.createTypeMap(Grade.class, GradeHistory.class);

        modelMapper.createTypeMap(GradeVersion.class, GradeVersionDto.class).addMappings(mapper -> {
            mapper.using(MapperConverter.toLocalDateTime).map(GradeVersion::getCreatedDate, GradeVersionDto::setCreatedDate);
            mapper.using(MapperConverter.toLocalDateTime).map(GradeVersion::getUpdatedDate, GradeVersionDto::setUpdatedDate);
        });
    }
}