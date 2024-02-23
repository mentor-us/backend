package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.domain.Group;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Map;

/**
 * The {@code MailService} interface defines methods for sending emails asynchronously.
 */
public interface MailService {

    /**
     * Asynchronously sends a plain text email.
     *
     * @param text    The content of the email.
     * @param subject The subject of the email.
     * @param to      The list of email addresses to which the email will be sent.
     */
    @Async
    void sendEmail(String text, String subject, List<String> to);

    /**
     * Asynchronously sends an email using a template.
     *
     * @param template   The template for the email content.
     * @param properties The properties to replace in the template.
     * @param subject    The subject of the email.
     * @param to         The list of email addresses to which the email will be sent.
     */
    @Async
    void sendEmailTemplate(String template, Map<String, Object> properties, String subject, List<String> to);

    /**
     * Asynchronously sends an invitation email to group.
     *
     * @param email The email address of the recipient.
     * @param group The group associated with the invitation.
     */
    @Async
    void sendInvitationToGroupMail(String email, Group group);

    /**
     * Asynchronously sends an invitation email.
     *
     * @param email The email address of the recipient.
     */
    @Async
    void sendWelcomeMail(String email);

}
