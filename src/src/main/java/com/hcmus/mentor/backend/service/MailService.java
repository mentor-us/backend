package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.entity.Email;
import com.hcmus.mentor.backend.entity.Group;
import com.hcmus.mentor.backend.entity.User;
import com.hcmus.mentor.backend.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class MailService {

    private static final Logger LOGGER = LogManager.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final UserRepository userRepository;

    @Value("${spring.mail.username}")
    private String sender;

    public MailService(JavaMailSender mailSender, SpringTemplateEngine templateEngine, UserRepository userRepository) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.userRepository = userRepository;
    }

    @Async
    public void sendSimpleMail(Email email) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(sender);
            mailMessage.setTo(email.getRecipient());
            mailMessage.setText(email.getMsgBody());
            mailMessage.setSubject(email.getSubject());
            //mailSender.send(mailMessage);
            LOGGER.info("Sended mail from {}, to {}", sender, email.getRecipient());
        } catch (Exception e) {
            LOGGER.info("Cannot send mail from {}, to {}", sender, email.getRecipient());
            e.printStackTrace();
        }
    }

    @Async
    public void sendHTMLMail(Email email) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
            Context context = new Context();
            context.setVariables(email.getProperties());
            helper.setFrom(sender);
            helper.setTo(email.getRecipient());
            helper.setSubject(email.getSubject());
            String html = templateEngine.process(email.getTemplate(), context);
            helper.setText(html, true);
            mailSender.send(message);
            LOGGER.info("Sended mail from {}, to {}", sender, email.getRecipient());
        } catch (Exception e) {
            LOGGER.info("Cannot send mail from {}, to {}", sender, email.getRecipient());
            e.printStackTrace();
        }
    }

    @Async
    public void sendInvitationMail(String emailAddress, Group group) {
        String username = emailAddress;
        Optional<User> userOptional = userRepository.findByEmail(emailAddress);

        if (userOptional.isPresent()) {
            username = userOptional.get().getName();
        }

        Map<String, Object> properties = new HashMap<>();
        properties.put("name", username);
        properties.put("groupName", group.getName());
        properties.put("groupId", group.getId());
        Email email = Email.builder()
                .recipient(emailAddress)
                .msgBody("Welcome to MentorUS app!")
                .subject("Invite to MentorUS")
                .properties(properties)
                .template("welcome-email.html")
                .build();
        sendHTMLMail(email);
    }

    public void sendTemplateMail(String emailAddress, Map<String, Object> properties, String subject, String template) {
        Email email = Email.builder()
                .recipient(emailAddress)
                .subject(subject)
                .properties(properties)
                .template(template)
                .build();
        sendHTMLMail(email);
    }
}
