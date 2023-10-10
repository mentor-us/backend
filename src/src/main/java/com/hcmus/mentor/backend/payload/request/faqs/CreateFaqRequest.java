package com.hcmus.mentor.backend.payload.request.faqs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateFaqRequest {

  private String question;

  private String answer;

  private String groupId;
}
