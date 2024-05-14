package com.hcmus.mentor.backend.controller.usecase.notification;

import com.hcmus.mentor.backend.domain.Notification;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationMapper(ModelMapper modelMapper){

        modelMapper.createTypeMap(Notification.class, NotificationMapper.class);
    }
}