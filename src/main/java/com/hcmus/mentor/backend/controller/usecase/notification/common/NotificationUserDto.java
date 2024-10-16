package com.hcmus.mentor.backend.controller.usecase.notification.common;

import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationUserDto {

    private String id;
    private Boolean isReaded = false;
    private Boolean isDeleted = false;
    private Boolean isAgreed;
    private ShortProfile user;

}
