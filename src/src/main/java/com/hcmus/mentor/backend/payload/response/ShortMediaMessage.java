package com.hcmus.mentor.backend.payload.response;

import com.hcmus.mentor.backend.entity.Message;
import com.hcmus.mentor.backend.payload.FileModel;
import com.hcmus.mentor.backend.payload.response.users.ProfileResponse;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShortMediaMessage {

  private String id;

  private ProfileResponse sender;

  private String imageUrl;

  private FileModel file;

  private Message.Type type;

  private Date createdDate;
}
