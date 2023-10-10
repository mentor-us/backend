package com.hcmus.mentor.backend.scheduler;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.hcmus.mentor.backend.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class QuartzConfig {
  //    @Bean
  //    public JobDetail saveRemindersJobDetail() {
  //        return JobBuilder.newJob()
  //                .ofType(SaveRemindersJob.class)
  //                .storeDurably()
  //                .withIdentity("saveRemindersJob")
  //                .build();
  //    }
  //
  //    @Bean
  //    public Trigger saveRemindersJobTrigger() {
  //        return TriggerBuilder.newTrigger()
  //                .forJob(saveRemindersJobDetail())
  //                .withIdentity("saveRemindersTrigger")
  //                .withSchedule(CronScheduleBuilder
  //                        .dailyAtHourAndMinute(0, 1)) // Schedule to run at 00:01 every day
  //                .build();
  //    }

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
        .withSchedule(
            SimpleScheduleBuilder.repeatMinutelyForever(5)) // Schedule to run every 5 minutes
        .build();
  }

  //    @Component
  //    public static class SaveRemindersJob implements Job {
  //        @Autowired
  //        private ReminderService reminderService;
  //
  //        @Override
  //        public void execute(JobExecutionContext context) throws JobExecutionException {
  //            reminderService.saveReminders();
  //        }
  //    }
  @Component
  @RequiredArgsConstructor
  public static class SendRemindersJob implements Job {

    private final ReminderService reminderService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
      try {
        reminderService.sendReminders();
      } catch (FirebaseMessagingException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
