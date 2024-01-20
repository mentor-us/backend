package com.hcmus.mentor.backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Email {

    String template;
    Map<String, Object> properties;
    private String sender;
    private String recipient;
    private String msgBody;
    private String subject;
}
