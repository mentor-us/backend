package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.domain.Email;
import com.hcmus.mentor.backend.domain.Group;

import java.util.Map;

import org.springframework.scheduling.annotation.Async;

public interface MailService {
    @Async
    void sendSimpleMail(Email email);

    @Async
    void sendHTMLMail(Email email);

    @Async
    void sendInvitationMail(String emailAddress, Group group);

    void sendTemplateMail(
            String emailAddress, Map<String, Object> properties, String subject, String template);
}
