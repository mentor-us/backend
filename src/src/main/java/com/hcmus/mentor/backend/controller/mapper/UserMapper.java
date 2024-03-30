package com.hcmus.mentor.backend.controller.mapper;

import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.domain.User;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = org.mapstruct.factory.Mappers.getMapper(UserMapper.class);

    ShortProfile userToShortProfile(User user);
}
