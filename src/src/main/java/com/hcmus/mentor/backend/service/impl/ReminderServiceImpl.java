package com.hcmus.mentor.backend.service.impl;

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
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ReminderServiceImpl implements ReminderService {
    private static final Logger logger = LogManager.getLogger(ReminderServiceImpl.class);
    private final FirebaseServiceImpl firebaseMessagingManager;
    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final MailService mailService;
    @Value("${app.frontendUrl}")
    private String frontendUrl;

    @Override
    public void sendReminders() {
        List<Reminder> reminders = reminderRepository.findByReminderDateBefore(new Date());

        for (Reminder reminder : reminders) {
            var receiverIds = getStrings(reminder, reminder.getType().toString() + "_REMINDER");
            var group = reminder.getGroup().getGroup();
            var title = group != null ? group.getName() : "MentorUS";
            var body = "";
            if (reminder.getType() == ReminderType.MEETING) {
                body = String.format("Lịch hẹn %s sẽ diễn ra lúc %s", reminder.getName(), reminder.getProperties().get("dueDate"));
            } else if (reminder.getType() == ReminderType.TASK) {
                body = String.format("Công việc %s sẽ tới hạn lúc %s", reminder.getName(), reminder.getProperties().get("dueDate"));
            }

            firebaseMessagingManager.sendNotificationMulticast(receiverIds, title, body, Collections.emptyMap());
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
        List<String> recipients = reminder.getRecipients().stream().map(User::getEmail).toList();
        List<String> receiverIds = new ArrayList<>();
        recipients.forEach(recipient -> {
            mailService.sendEmailTemplate(template, properties, subject, Collections.singletonList(recipient));
            logger.info("Send email reminder {} to {}", reminder.getType(), recipient);

            Optional<User> userOptional = userRepository.findByEmail(recipient);
            userOptional.ifPresent(user -> receiverIds.add(user.getId()));
        });
        return receiverIds;
    }
}