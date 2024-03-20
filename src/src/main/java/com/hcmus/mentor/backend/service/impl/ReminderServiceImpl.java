package com.hcmus.mentor.backend.service.impl;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.Reminder;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.ReminderType;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.ReminderRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.service.MailService;
import com.hcmus.mentor.backend.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ReminderServiceImpl implements ReminderService {
    private static final Logger logger = LogManager.getLogger(ReminderServiceImpl.class);
    private final FirebaseMessagingServiceImpl firebaseMessagingManager;
    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final MailService mailService;
    @Value("${app.frontendUrl}")
    private String frontendUrl;

    @Override
    public void sendReminders() throws FirebaseMessagingException {
        List<Reminder> reminders = reminderRepository.findByReminderDateBefore(new Date());

        for (Reminder reminder : reminders) {
            String template = reminder.getType().toString() + "_REMINDER";
            List<String> receiverIds = getStrings(reminder, template);
            Optional<Group> groupOptional = groupRepository.findById(reminder.getGroupId());
            String title = groupOptional.isPresent() ? groupOptional.get().getName() : "MentorUS";
            String body = "";
            if (reminder.getType() == ReminderType.MEETING) {
                body = String.format("Lịch hẹn %s sẽ diễn ra lúc %s", reminder.getName(), reminder.getProperties().get("dueDate"));
            } else if (reminder.getType() == ReminderType.TASK) {
                body = String.format("Công việc %s sẽ tới hạn lúc %s", reminder.getName(), reminder.getProperties().get("dueDate"));
            }
            firebaseMessagingManager.sendGroupNotification(receiverIds, title, body);
        }

        if (!reminders.isEmpty()) {
            reminderRepository.deleteAll(reminders);
        }
    }

    @NotNull
    private List<String> getStrings(Reminder reminder, String template) {
        String subject = reminder.getSubject();
        Map<String, Object> properties = reminder.getProperties();
        properties.put("frontendUrl", frontendUrl);
        List<String> recipients = reminder.getRecipients();
        List<String> receiverIds = new ArrayList<>();
        recipients.forEach(recipient -> {
            mailService.sendEmailTemplate(template, properties, subject, Collections.singletonList(recipient));
            logger.info("Send reminder {} to {}", reminder.getType(), recipient);

            Optional<User> userOptional = userRepository.findByEmail(recipient);
            userOptional.ifPresent(user -> receiverIds.add(user.getId()));
        });
        return receiverIds;
    }
}
