package com.hcmus.mentor.backend.controller.payload.request.faqs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateFaqRequest {

    @Length(min = 1, max = 255, message ="Câu hỏi không vượt quá 255 ký tự.")
    private String question;

    private String answer;

    private String groupId;
}
