package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.entity.*;
import com.hcmus.mentor.backend.payload.request.meetings.CreateMeetingRequest;
import com.hcmus.mentor.backend.payload.request.RescheduleMeetingRequest;
import com.hcmus.mentor.backend.payload.request.meetings.UpdateMeetingRequest;
import com.hcmus.mentor.backend.payload.response.meetings.MeetingAttendeeResponse;
import com.hcmus.mentor.backend.payload.response.meetings.MeetingDetailResponse;
import com.hcmus.mentor.backend.payload.response.meetings.MeetingHistoryDetail;
import com.hcmus.mentor.backend.payload.response.meetings.MeetingResponse;
import com.hcmus.mentor.backend.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.payload.response.messages.MessageResponse;
import com.hcmus.mentor.backend.payload.response.users.ProfileResponse;
import com.hcmus.mentor.backend.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.MeetingRepository;
import com.hcmus.mentor.backend.repository.ReminderRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.util.DateUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MeetingService implements IRemindableService {

    private final MeetingRepository meetingRepository;

    private final UserRepository userRepository;

    private final GroupRepository groupRepository;

    private final MongoTemplate mongoTemplate;

    private final GroupService groupService;

    private final MessageService messageService;

    private final SocketIOService socketIOService;

    private final ReminderRepository reminderRepository;

    private final NotificationService notificationService;

    public MeetingService(MeetingRepository meetingRepository, UserRepository userRepository, GroupRepository groupRepository, MongoTemplate mongoTemplate, GroupService groupService, MessageService messageService, SocketIOService socketIOService, ReminderRepository reminderRepository, NotificationService notificationService) {
        this.meetingRepository = meetingRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.mongoTemplate = mongoTemplate;
        this.groupService = groupService;
        this.messageService = messageService;
        this.socketIOService = socketIOService;
        this.reminderRepository = reminderRepository;
        this.notificationService = notificationService;
    }

    public List<MeetingResponse> getMostRecentMeetings(String userId) {
//        Date now = new Date();
//        MatchOperation recent = Aggregation.match(Criteria
//                .where("attendees").in(userId)
//                .and("timeStart").gte(now));
//        ProjectionOperation toObjectId = Aggregation.project("title", "description", "timeStart", "timeEnd",
//                        "repeated", "place")
//                .and(ConvertOperators.ToObjectId.toObjectId("$groupId")).as("groupId")
//                .and(ConvertOperators.ToObjectId.toObjectId("$organizerId")).as("organizerId");
//        ProjectionOperation mapGroupAndOrganizer = Aggregation.project("title", "description", "timeStart", "timeEnd",
//                        "repeated", "place")
//                .and(ArrayOperators.arrayOf("groups").elementAt(0)).as("group")
//                .and(ArrayOperators.arrayOf("organizers").elementAt(0)).as("organizer");
//        Aggregation aggregation = Aggregation.newAggregation(
//                recent,
//                toObjectId,
//                Aggregation.lookup("group", "groupId", "_id", "groups"),
//                Aggregation.lookup("user", "organizerId", "_id", "organizers"),
//                Aggregation.sort(Sort.Direction.ASC, "timeStart"),
//                mapGroupAndOrganizer,
//                Aggregation.limit(5)
//        );
//        List<MeetingResponse> meetings = mongoTemplate.aggregate(aggregation, "meeting", MeetingResponse.class)
//                .getMappedResults();

        List<String> groupIds = groupService.getAllActiveOwnGroups(userId)
                .stream().map(Group::getId).collect(Collectors.toList());
        Date now = new Date();
        List<Meeting> meetings = meetingRepository.findAllByGroupIdInAndOrganizerIdAndTimeStartGreaterThanOrGroupIdInAndAttendeesInAndTimeStartGreaterThan(
                groupIds,
                userId,
                now,
                groupIds,
                Arrays.asList("*", userId),
                now,
                PageRequest.of(0, 5, Sort.by("timeStart").descending())
        ).getContent();
        return meetings.stream()
                .map(meeting -> {
                    Group group = groupRepository.findById(meeting.getGroupId()).orElse(null);
                    User organizer = userRepository.findById(meeting.getOrganizerId()).orElse(null);
                    return MeetingResponse.from(meeting, organizer, group);
                }).collect(Collectors.toList());
    }

    public List<MeetingResponse> getMeetingGroup(String groupId) {
        return meetingRepository.findAllByGroupId(groupId);
    }

    public List<Meeting> getMeetingGroup(String groupId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Meeting> wrapper = meetingRepository.findByGroupId(groupId, pageRequest);
        return wrapper.getContent();
    }

    public Meeting createNewMeeting(CreateMeetingRequest request) {
        User organizer = userRepository.findById(request.getOrganizerId())
                .orElse(null);
        if (organizer == null) {
            return null;
        }

        Meeting newMeeting = meetingRepository.save(Meeting.from(request));
        Message newMessage = Message.builder()
                .senderId(request.getOrganizerId())
                .content("NEW MEETING")
                .createdDate(new Date())
                .type(Message.Type.MEETING)
                .groupId(request.getGroupId())
                .meetingId(newMeeting.getId())
                .build();
        messageService.saveMessage(newMessage);
        groupService.pingGroup(request.getGroupId());

        MessageDetailResponse response = messageService
                .fulfillMeetingMessage(MessageResponse.from(newMessage, ProfileResponse.from(organizer)));
        socketIOService.sendBroadcastMessage(response, request.getGroupId());
        notificationService.sendNewMeetingNotification(response);
        saveToReminder(newMeeting);

        return newMeeting;
    }

    public Meeting updateMeeting(String modifierId, String meetingId, UpdateMeetingRequest request) {
        Optional<Meeting> wrapper = meetingRepository.findById(meetingId);
        if (!wrapper.isPresent()) {
            return null;
        }

        Meeting meeting = wrapper.get();
        if (!isEqualDate(meeting.getTimeStart(), request.getTimeStart())
                || !isEqualDate(meeting.getTimeEnd(), request.getTimeEnd())) {
            meeting.reschedule(modifierId, request);
        }
        meeting.update(request);

        groupService.pingGroup(meeting.getGroupId());

        Reminder reminder = reminderRepository.findByRemindableId(meetingId);
        if (reminder != null){
            reminder.setReminderDate(meeting.getReminderDate());
            reminderRepository.save(reminder);
        }

        return meetingRepository.save(meeting);
    }

    private boolean isEqualDate(Date oldDate, Date newDate) {
        Calendar oldTime = Calendar.getInstance();
        oldTime.setTime(oldDate);
        Calendar newTime = Calendar.getInstance();
        newTime.setTime(newDate);
        return oldTime.equals(newTime);
    }

    public void deleteMeeting(String meetingId) {
        meetingRepository.deleteById(meetingId);
        reminderRepository.deleteByRemindableId(meetingId);
    }

    public MeetingDetailResponse getMeetingById(String userId, String meetingId) {
        Optional<Meeting> wrapper = meetingRepository.findById(meetingId);
        if (!wrapper.isPresent()) {
            return null;
        }
        Meeting meeting = wrapper.get();

        Optional<User> organizerWrapper = userRepository.findById(meeting.getOrganizerId());
        if (!organizerWrapper.isPresent()) {
            return null;
        }
        User organizer = organizerWrapper.get();

        Optional<Group> groupWrapper = groupRepository.findById(meeting.getGroupId());
        if (!groupWrapper.isPresent()) {
            return null;
        }
        Group group = groupWrapper.get();

        Map<String, ShortProfile> modifiers = meeting.getHistories()
                .stream()
                .map(Meeting.MeetingHistory::getModifierId)
                .map(userRepository::findShortProfile)
                .collect(Collectors.toMap(ShortProfile::getId, profile -> profile, (p1, p2) -> p2));
        List<MeetingHistoryDetail> historyDetails = meeting.getHistories()
                .stream()
                .map(history -> {
                    ShortProfile user = modifiers.getOrDefault(history.getModifierId(), null);
                    return MeetingHistoryDetail.from(history, user);
                })
                .collect(Collectors.toList());

        boolean appliedAllGroup = meeting.getAttendees().contains("*");
        return MeetingDetailResponse.builder()
                .id(meetingId)
                .title(meeting.getTitle())
                .description(meeting.getDescription())
                .timeStart(meeting.getTimeStart())
                .timeEnd(meeting.getTimeEnd())
                .repeated(meeting.getRepeated())
                .place(meeting.getPlace())
                .organizer(organizer)
                .group(group)
                .isAll(appliedAllGroup)
                .canEdit(group.isMentor(userId) || organizer.getId().equals(userId))
                .totalAttendees(appliedAllGroup
                        ? group.getTotalMember() - 1
                        : meeting.getAttendees().size())
                .histories(historyDetails)
                .build();
    }

    public List<MeetingAttendeeResponse> getMeetingAttendees(String meetingId) {
        Optional<Meeting> wrapper = meetingRepository.findById(meetingId);
        if (!wrapper.isPresent()) {
            return Collections.emptyList();
        }
        Meeting meeting = wrapper.get();

        Optional<Group> groupWrapper = groupRepository.findById(meeting.getGroupId());
        if (!groupWrapper.isPresent()) {
            return Collections.emptyList();
        }
        Group group = groupWrapper.get();

        List<String> attendeeIds;
        boolean appliedAllGroup = meeting.getAttendees().contains("*");
        if (appliedAllGroup) {
            attendeeIds = Stream.concat(group.getMentees().stream(),
                            group.getMentors().stream())
                    .filter(id -> !id.equals(meeting.getOrganizerId()))
                    .collect(Collectors.toList());
        } else {
            attendeeIds = meeting.getAttendees();
        }
        List<MeetingAttendeeResponse> attendees = userRepository.findByIdIn(attendeeIds).stream()
                .map(user -> MeetingAttendeeResponse.from(user, group.isMentor(user.getId())))
                .collect(Collectors.toList());
        return attendees;
    }

    public List<Meeting> getAllOwnMeetings(String userId) {
        List<String> joinedGroupIds = groupService.getAllActiveOwnGroups(userId)
                .stream().map(Group::getId).collect(Collectors.toList());
        return meetingRepository.findAllByGroupIdInAndOrganizerIdOrGroupIdInAndAttendeesIn(
                joinedGroupIds,
                userId,
                joinedGroupIds,
                Arrays.asList("*", userId)
        );
    }

    public List<Meeting> getAllOwnMeetingsByDate(String userId, Date date) {
        Date startTime = DateUtils.atStartOfDay(date);
        Date endTime = DateUtils.atEndOfDay(date);
        return getAllOwnMeetingsBetween(userId, startTime, endTime);
    }

    public List<Meeting> getAllOwnMeetingsBetween(String userId, Date startTime, Date endTime) {
        List<String> joinedGroupIds = groupService.getAllActiveOwnGroups(userId)
                .stream().map(Group::getId).collect(Collectors.toList());
        List<Meeting> organizedMeetings = meetingRepository.findAllByGroupIdInAndOrganizerIdAndTimeStartGreaterThanEqualAndTimeEndLessThanEqual(
                joinedGroupIds,
                userId,
                startTime,
                endTime
        );
        List<Meeting> attendeeMeetings = meetingRepository.findAllByGroupIdInAndAttendeesInAndTimeStartGreaterThanEqualAndTimeEndLessThanEqual(
                joinedGroupIds,
                Arrays.asList("*", userId),
                startTime,
                endTime
        );
        return Stream.concat(organizedMeetings.stream(), attendeeMeetings.stream())
                .collect(Collectors.toList());
    }

    public List<Meeting> getAllOwnMeetingsByMonth(String userId, Date date) {
        Date startTime = DateUtils.atStartOfMonth(date);
        Date endTime = DateUtils.atEndOfMonth(date);
        return getAllOwnMeetingsBetween(userId, startTime, endTime);
    }

    public Meeting rescheduleMeeting(String modifierId, String meetingId, RescheduleMeetingRequest request) {
        Optional<Meeting> meetingWrapper = meetingRepository.findById(meetingId);
        if (!meetingWrapper.isPresent()) {
            return null;
        }

        Meeting meeting = meetingWrapper.get();
        meeting.reschedule(modifierId, request);
        groupService.pingGroup(meeting.getGroupId());
        return meetingRepository.save(meeting);
     }
      
//    @Override
//    public List<Reminder> findReminderToday() {
//        Date now = new Date();
//        // Create a Calendar instance and set it to the current date and time
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(now);
//
//        // Add one day to the current date to get tomorrow's date
//        calendar.add(Calendar.DAY_OF_MONTH, 1);
//
//        // Get the date for tomorrow
//        Date tomorrow = calendar.getTime();
//        List<Meeting> meetings = meetingRepository.findAllByTimeStartBetween(now, tomorrow);
//        List<Reminder> reminders = new ArrayList<>();
//        for(Meeting meeting: meetings){
//            Reminder reminder = meeting.toReminder();
//            List<String> emailUsers = new ArrayList<>();
//            for(String userId: meeting.getAttendees()){
//                Optional<User> userOptional = userRepository.findById(userId);
//                if(userOptional.isPresent()){
//                    emailUsers.add(userOptional.get().getEmail());
//                }
//            }
//            reminder.setRecipients(emailUsers);
//            reminders.add(reminder);
//        }
//        return reminders;
//    }

    @Override
    public void saveToReminder(IRemindable remindable) {
        Reminder reminder = remindable.toReminder();
        Meeting meeting = (Meeting) remindable;
        List<String> emailUsers = new ArrayList<>();
        List<String> attendees = meeting.getAttendees();
        attendees.add(meeting.getOrganizerId());
        for(String userId: attendees){
            Optional<User> userOptional = userRepository.findById(userId);
            if(userOptional.isPresent()){
                emailUsers.add(userOptional.get().getEmail());
            }
        }
        reminder.setRecipients(emailUsers);
        reminder.setSubject("Bạn có 1 lịch hẹn sắp diễn ra");

        reminderRepository.save(reminder);
    }
}
