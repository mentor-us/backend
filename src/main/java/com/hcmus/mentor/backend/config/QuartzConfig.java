package com.hcmus.mentor.backend.config;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.hcmus.mentor.backend.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail sendRemindersJobDetail() {
        return JobBuilder.newJob()
                .ofType(SendRemindersJob.class)
                .storeDurably()
                .withIdentity("sendRemindersJob")
                .build();
    }

    @Bean
    public Trigger sendRemindersJobTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(sendRemindersJobDetail())
                .withIdentity("sendRemindersTrigger")
                .withSchedule(SimpleScheduleBuilder.repeatMinutelyForever(5)) // Schedule to run every 5 minutes
                .build();
    }

    @Component
    @RequiredArgsConstructor
    public static class SendRemindersJob implements Job {

        private final ReminderService reminderService;

        @Override
        public void execute(JobExecutionContext context) {
            try {
                reminderService.sendReminders();
            } catch (FirebaseMessagingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
