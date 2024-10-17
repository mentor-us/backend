package vn.edu.hcmus.mentor.backend.controller.usecase.notification.common;

import vn.edu.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import vn.edu.hcmus.mentor.backend.domain.constant.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDetailDto {

    private String id;
    private String title;
    private String content;
    private NotificationType type;
    private ShortProfile sender;
    private Date createdDate;
    private String refId;
    private List<NotificationUserDto> receivers = new ArrayList<>();

}
