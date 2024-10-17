package vn.edu.hcmus.mentor.controller.usecase.grade;

import vn.edu.hcmus.mentor.controller.usecase.common.mapper.MapperConverter;
import vn.edu.hcmus.mentor.controller.usecase.grade.common.GradeDto;
import vn.edu.hcmus.mentor.controller.usecase.grade.common.GradeVersionDto;
import vn.edu.hcmus.mentor.domain.Grade;
import vn.edu.hcmus.mentor.domain.GradeHistory;
import vn.edu.hcmus.mentor.domain.GradeVersion;
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