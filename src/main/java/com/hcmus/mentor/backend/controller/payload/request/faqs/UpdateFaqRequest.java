package com.hcmus.mentor.backend.controller.payload.request.faqs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateFaqRequest {

    private String question;

    private String answer;

    private String groupId;
}
