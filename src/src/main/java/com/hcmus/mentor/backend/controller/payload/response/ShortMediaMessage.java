package com.hcmus.mentor.backend.controller.payload.response;

import com.hcmus.mentor.backend.controller.payload.FileModel;
import com.hcmus.mentor.backend.controller.payload.response.users.ProfileResponse;
import com.hcmus.mentor.backend.domain.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortMediaMessage {

    private String id;

    private ProfileResponse sender;

    private String imageUrl;

    private FileModel file;

    private Message.Type type;

    private LocalDateTime createdDate;
}
