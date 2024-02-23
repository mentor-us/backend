package com.hcmus.mentor.backend.service.impl;

import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.service.MailService;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.List;
import java.util.Map;

/**
 * {@inheritDoc}
 */
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private static final Logger logger = LogManager.getLogger(MailServiceImpl.class);
    private final SpringTemplateEngine templateEngine;
    private final JavaMailSender emailSender;
    private final UserRepository userRepository;
    @Value("${spring.mail.username}")
    private String sender;
    @Value("${app.frontendUrl}")
    private String frontendUrl;

    /**
     * {@inheritDoc}
     */
    @Async
    public void sendEmail(String text, String subject, List<String> to) {
        var message = emailSender.createMimeMessage();
        try {
            setSubjectAndRecipients(message, subject, to);

            var processText = text.trim();
            message.setContent(processText, processText.startsWith("<") && processText.endsWith(">") ? "text/html" : "text/plain");

            emailSender.send(message);
        } catch (Exception ex) {
            logger.error("MailService::sendEmail", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Async
    public void sendEmailTemplate(String template, Map<String, Object> properties, String subject, List<String> to) {
        var message = emailSender.createMimeMessage();
        try {
            setSubjectAndRecipients(message, subject, to);

            final var context = new Context();
            context.setVariables(properties);
            var text = templateEngine.process(template, context).trim();

            message.setText(text, "utf-8", "html");

            emailSender.send(message);
        } catch (Exception ex) {
            logger.error("MailService::sendEmailTemplate", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Async
    public void sendInvitationToGroupMail(String email, Group group) {
        var userOpt = userRepository.findByEmail(email);
        var name = userOpt.map(User::getName).orElse(email);

        this.sendEmailTemplate(
                "mails/welcome-to-app.html",
                Map.of("name", name, "groupName", group.getName(), "groupId", group.getId(), "frontendUrl", frontendUrl),
                String.format("You have been invited to join group %s !", group.getName()),
                List.of(email));

        logger.info("Invitation email sent to {}, groupId::{}, groupName::{}", email, group.getId(), group.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendWelcomeMail(String email) {
        var userOpt = userRepository.findByEmail(email);
        var name = userOpt.map(User::getName).orElse(email);

        this.sendEmailTemplate(
                "mails/welcome-to-app.html",
                Map.of("name", name, "frontendUrl", frontendUrl),
                "Welcome to MentorUS!",
                List.of(email));

        logger.info("Welcome email sent to {}", email);
    }

    private void setSubjectAndRecipients(MimeMessage message, String subject, List<String> to) throws MessagingException, DomainException {
        message.setFrom(sender);

        if (to == null || to.isEmpty()) {
            throw new DomainException("List of email recipients is empty.");
        }

        for (var email : to) {
            message.setRecipients(Message.RecipientType.TO, email);
        }

        message.setSubject(subject);
    }
}
