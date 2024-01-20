package com.hcmus.mentor.backend.service.impl;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.Reminder;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.ReminderRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.service.MailService;
import com.hcmus.mentor.backend.service.ReminderService;

import java.util.*;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReminderServiceImpl implements ReminderService {
    private static final Logger LOGGER = LogManager.getLogger(ReminderServiceImpl.class);
    private final FirebaseMessagingServiceImpl firebaseMessagingManager;
    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final MailService mailService;

    //    public List<Reminder> getReminders(IRemindableService remindableService){
    //        return remindableService.findReminderToday();
    //    }

    //    public List<Reminder> saveReminders(){
    //        List<Reminder> reminders = new ArrayList<>();
    //        List<Reminder> meetingReminders = getReminders(meetingService);
    //        List<Reminder> taskReminders = getReminders(taskService);
    //
    //        reminders.addAll(meetingReminders);
    //        reminders.addAll(taskReminders);
    //
    //        reminderRepository.saveAll(reminders);
    //
    //        return reminders;
    //    }

    @Override
    public void sendReminders() throws FirebaseMessagingException {
        List<Reminder> reminders = reminderRepository.findByReminderDateBefore(new Date());

        for (Reminder reminder : reminders) {
            String template = reminder.getType().toString() + "_REMINDER";
            String subject = reminder.getSubject();
            Map<String, Object> properties = reminder.getProperties();
            List<String> recipients = reminder.getRecipients();
            List<String> receiverIds = new ArrayList<>();
            recipients.forEach(
                    recipient -> {
                        LOGGER.info("Send reminder " + reminder.getType() + " to " + recipient);
                        mailService.sendTemplateMail(recipient, properties, subject, template);
                        Optional<User> userOptional = userRepository.findByEmail(recipient);
                        userOptional.ifPresent(user -> receiverIds.add(user.getId()));
                    });
            Optional<Group> groupOptional = groupRepository.findById(reminder.getGroupId());
            String title = groupOptional.isPresent() ? groupOptional.get().getName() : "MentorUS";
            String body = "";
            if (reminder.getType() == Reminder.ReminderType.MEETING) {
                body =
                        "Lịch hẹn "
                                + reminder.getName()
                                + " sẽ diễn ra lúc "
                                + reminder.getProperties().get("dueDate");
            } else if (reminder.getType() == Reminder.ReminderType.TASK) {
                body =
                        "Công việc "
                                + reminder.getName()
                                + " sẽ tới hạn lúc "
                                + reminder.getProperties().get("dueDate");
            }
            firebaseMessagingManager.sendGroupNotification(receiverIds, title, body);
        }
        reminderRepository.deleteAll(reminders);
    }
}
