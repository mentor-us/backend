package com.hcmus.mentor.backend.controller.usecase.notification;

import com.hcmus.mentor.backend.controller.payload.response.NotificationResponse;
import com.hcmus.mentor.backend.controller.usecase.notification.common.NotificationDetailDto;
import com.hcmus.mentor.backend.domain.Notification;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationMapper(ModelMapper modelMapper) {
        modelMapper.emptyTypeMap(Notification.class, NotificationResponse.class).addMappings(mapper -> {
            mapper.skip(NotificationResponse::setSender);
        }).implicitMappings();
        modelMapper.emptyTypeMap(Notification.class, NotificationDetailDto.class).addMappings(mapper -> {
        }).implicitMappings();
    }
}