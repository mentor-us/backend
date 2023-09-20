package com.hcmus.mentor.backend.entity;

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

    private String sender;

    private String recipient;

    private String msgBody;

    private String subject;
    String template;
    Map<String, Object> properties;
}
