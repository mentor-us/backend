package com.hcmus.mentor.backend.controller.mapper;

import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.domain.User;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

   public UserMapper(ModelMapper modelMapper) {
       modelMapper.createTypeMap(User.class, ShortProfile.class);
   }
}