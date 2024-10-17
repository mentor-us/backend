package vn.edu.hcmus.mentor.controller.usecase.notification;

import vn.edu.hcmus.mentor.controller.payload.response.NotificationResponse;
import vn.edu.hcmus.mentor.controller.usecase.notification.common.NotificationDetailDto;
import vn.edu.hcmus.mentor.domain.Notification;
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