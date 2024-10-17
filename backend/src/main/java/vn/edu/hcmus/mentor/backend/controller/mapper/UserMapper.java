package vn.edu.hcmus.mentor.backend.controller.mapper;

import vn.edu.hcmus.mentor.backend.controller.payload.response.users.ProfileResponse;
import vn.edu.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import vn.edu.hcmus.mentor.backend.controller.usecase.grade.common.GradeUserProfile;
import vn.edu.hcmus.mentor.backend.controller.usecase.grade.common.GradeUserDto;
import vn.edu.hcmus.mentor.backend.controller.usecase.user.searchmenteesofuser.ShortMenteeProfile;
import vn.edu.hcmus.mentor.backend.domain.User;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserMapper(ModelMapper modelMapper) {

        modelMapper.createTypeMap(User.class, ShortProfile.class);
        modelMapper.createTypeMap(User.class, ProfileResponse.class);
        modelMapper.createTypeMap(User.class, ShortMenteeProfile.class);
        modelMapper.createTypeMap(User.class, GradeUserProfile.class);
        modelMapper.createTypeMap(User.class, GradeUserDto.class).addMappings(mapper -> {
            mapper.map(User::getId, GradeUserDto::setUserId);
            mapper.map(User::getGradeShareType, GradeUserDto::setShareType);
        });
    }
}