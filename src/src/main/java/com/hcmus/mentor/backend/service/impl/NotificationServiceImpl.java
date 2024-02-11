package com.hcmus.mentor.backend.service.impl;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.hcmus.mentor.backend.controller.payload.request.AddNotificationRequest;
import com.hcmus.mentor.backend.controller.payload.request.RescheduleMeetingRequest;
import com.hcmus.mentor.backend.controller.payload.request.SubscribeNotificationRequest;
import com.hcmus.mentor.backend.controller.payload.response.NotificationResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.ReactMessageResponse;
import com.hcmus.mentor.backend.controller.payload.response.tasks.TaskAssigneeResponse;
import com.hcmus.mentor.backend.controller.payload.response.tasks.TaskMessageResponse;
import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.domain.*;
import com.hcmus.mentor.backend.domain.constant.ChannelType;
import com.hcmus.mentor.backend.domain.constant.NotificationType;
import com.hcmus.mentor.backend.repository.*;
import com.hcmus.mentor.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hcmus.mentor.backend.domain.constant.NotificationType.*;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LogManager.getLogger(NotificationServiceImpl.class);
    private final NotificationRepository notificationRepository;
    private final NotificationSubscriberRepository notificationSubscriberRepository;
    private final GroupRepository groupRepository;
    private final FirebaseMessagingServiceImpl firebaseMessagingManager;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    @Override
    public Map<String, Object> getOwnNotifications(String userId, int page, int size) {
        PageRequest paging = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Slice<Notification> slice =
                notificationRepository.findByReceiverIdsIn(Collections.singletonList(userId), paging);
        List<String> senderIds = slice.get().map(Notification::getSenderId).toList();
        Map<String, ShortProfile> senders =
                userRepository.findByIds(senderIds).stream()
                        .collect(Collectors.toMap(ShortProfile::getId, profile -> profile, (p1, p2) -> p2));
        List<NotificationResponse> notifications =
                slice.getContent().stream()
                        .map(
                                notification ->
                                        NotificationResponse.from(
                                                notification, senders.getOrDefault(notification.getSenderId(), null)))
                        .toList();
        return pagingResponse(slice, notifications);
    }

    private Map<String, Object> pagingResponse(
            Slice<Notification> slice, List<NotificationResponse> notifications) {
        Map<String, Object> response = new HashMap<>();
        response.put("notifications", notifications);
        response.put("hasMore", slice.hasNext());
        return response;
    }

    @Override
    public Notification createResponseNotification(String senderId, AddNotificationRequest request) {
        Notification notif = Notification.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .type(NotificationType.NEED_RESPONSE)
                .senderId(senderId)
                .receiverIds(Collections.singletonList(request.getReceiverId()))
                .createdDate(request.getCreatedDate())
                .build();
        return notificationRepository.save(notif);
    }

    @Override
    public Notification responseNotification(String userId, String notificationId, String action) {
        Optional<Notification> notificationWrapper = notificationRepository.findById(notificationId);
        if (!notificationWrapper.isPresent()) {
            return null;
        }

        Notification notif = notificationWrapper.get();
        if (!notif.getType().equals(NotificationType.NEED_RESPONSE)) {
            return null;
        }

        switch (action) {
            case "seen":
                notif.seen(userId);
                break;
            case "accept":
                notif.accept(userId);
                break;
            case "refuse":
                notif.refuse(userId);
                break;
            default:
                break;
        }
        return notificationRepository.save(notif);
    }

    @Override
    public void subscribeNotification(SubscribeNotificationRequest request) {
        if (request == null) {
            return;
        }

        unsubscribeNotification(request.getUserId());

        if (request.getToken().isEmpty()) {
            logger.info("[*] Unsubscribe user notification: userID({})", request.getUserId());
            return;
        }
        logger.info(
                "[*] Subscribe user notification: userID({}) | Token({})",
                request.getUserId(),
                request.getToken());

        List<NotificationSubscriber> subscribes =
                notificationSubscriberRepository.findByUserIdOrToken(
                        request.getUserId(), request.getToken());
        if (subscribes.isEmpty()) {
            NotificationSubscriber subscriber =
                    NotificationSubscriber.builder()
                            .userId(request.getUserId())
                            .token(request.getToken())
                            .build();
            notificationSubscriberRepository.save(subscriber);
            return;
        }

        subscribes.forEach(subscriber -> subscriber.update(request));
        notificationSubscriberRepository.saveAll(subscribes);
    }

    @Override
    public void unsubscribeNotification(String userId) {
        notificationSubscriberRepository.deleteByUserId(userId);
    }

    @Override
    @Async
    public void sendNewMessageNotification(MessageDetailResponse message) {
        String title;
        List<String> members;

        Optional<Group> groupWrapper = groupRepository.findById(message.getGroupId());
        if (!groupWrapper.isPresent()) {
            Optional<Channel> channelWrapper = channelRepository.findById(message.getGroupId());
            if (!channelWrapper.isPresent()) {
                return;
            }

            Channel channel = channelWrapper.get();
            Group parentGroup = groupRepository.findById(channel.getParentId()).orElse(null);
            title = ChannelType.PRIVATE_MESSAGE.equals(channel.getType())
                    ? parentGroup.getName() + "\n" + message.getSender().getName()
                    : channel.getName();
            members = channel.getUserIds().stream()
                    .filter(id -> !id.equals(message.getSender().getId()))
                    .distinct()
                    .toList();
        } else {
            Group group = groupWrapper.get();
            title = group.getName();
            members = Stream.concat(group.getMentors().stream(), group.getMentees().stream())
                    .filter(id -> !id.equals(message.getSender().getId()))
                    .distinct()
                    .toList();
        }

        if (members.isEmpty()) {
            return;
        }

        Map<String, String> data = attachDataNotification(message.getGroupId(), NEW_MESSAGE);
        String senderName = "";
        if (message.getSender() != null) {
            senderName = message.getSender().getName();
            data.put("sender", senderName);
            String imageUrl =
                    message.getSender().getImageUrl() == null
                            || ("https://graph.microsoft.com/v1.0/me/photo/$value")
                            .equals(message.getSender().getImageUrl())
                            ? ""
                            : message.getSender().getImageUrl();
            data.put("imageUrl", imageUrl);
        }

        String body = senderName + ": " + Jsoup.parse(message.getContent()).text();
        try {
            firebaseMessagingManager.sendGroupNotification(members, title, body, data);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> attachDataNotification(String groupId, NotificationType type) {
        Map<String, String> data = new HashMap<>();
        data.put("type", type.name());
        data.put("screen", "chat");
        data.put("groupId", groupId);
        return data;
    }

    @Override
    @Async
    public void sendNewTaskNotification(MessageDetailResponse message) {
        if (message.getTask() == null) {
            logger.warn("[!] Task message #{}, NULL cannot send notifications", message.getId());
            return;
        }
        TaskMessageResponse task = message.getTask();

        Optional<Group> groupWrapper = groupRepository.findById(message.getGroupId());
        if (!groupWrapper.isPresent()) {
            return;
        }
        Group group = groupWrapper.get();

        String title = group.getName();
        String content = "Nhóm có công việc mới \"" + task.getTitle() + "\"";
        Notification notif = createNewTaskNotification(title, content, message.getSender().getId(), task);
        try {
            firebaseMessagingManager.sendGroupNotification(
                    notif.getReceiverIds(),
                    title,
                    content,
                    attachDataNotification(message.getGroupId(), NEW_TASK));
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Notification createNewTaskNotification(
            String title, String content, String senderId, TaskMessageResponse task) {
        List<String> assigneeIds =
                task.getAssignees().stream().map(TaskAssigneeResponse::getId).toList();
        List<String> receiverIds =
                Stream.concat(assigneeIds.stream(), Stream.of(task.getAssignerId()))
                        .distinct()
                        .toList();
        Notification inAppNotification =
                Notification.builder()
                        .title(title)
                        .content(content)
                        .type(NEW_TASK)
                        .senderId(senderId)
                        .refId(task.getId())
                        .receiverIds(receiverIds)
                        .build();
        return notificationRepository.save(inAppNotification);
    }

    @Override
    @Async
    public void sendNewMeetingNotification(MessageDetailResponse message) {
        if (message.getMeeting() == null) {
            logger.warn("[!] Meeting message #{}, NULL cannot send notifications", message.getId());
            return;
        }
        Meeting meeting = message.getMeeting();

        Optional<Group> groupWrapper = groupRepository.findById(message.getGroupId());
        if (!groupWrapper.isPresent()) {
            return;
        }
        Group group = groupWrapper.get();

        String title = group.getName();
        String content = "Nhóm có lịch hẹn mới \"" + meeting.getTitle() + "\"";
        Notification notif =
                createNewMeetingNotification(title, content, message.getSender().getId(), meeting);
        try {
            firebaseMessagingManager.sendGroupNotification(
                    notif.getReceiverIds(),
                    title,
                    content,
                    attachDataNotification(message.getGroupId(), NEW_MEETING));
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Notification createNewMeetingNotification(
            String title, String content, String senderId, Meeting meeting) {
        List<String> receiverIds =
                Stream.concat(meeting.getAttendees().stream(), Stream.of(meeting.getOrganizerId()))
                        .distinct()
                        .toList();
        Notification notif =
                Notification.builder()
                        .title(title)
                        .content(content)
                        .type(NEW_MEETING)
                        .senderId(senderId)
                        .refId(meeting.getId())
                        .receiverIds(receiverIds)
                        .build();
        return notificationRepository.save(notif);
    }

    @Override
    @Async
    public void sendNewMediaMessageNotification(MessageDetailResponse message) {
        boolean isImageMessage = Message.Type.IMAGE.equals(message.getType())
                && message.getImages() != null
                && !message.getImages().isEmpty();
        boolean isFileMessage = Message.Type.FILE.equals(message.getType()) && message.getFile() != null;
        if (!isImageMessage && !isFileMessage) {
            logger.warn("[!] Media message #{}, empty cannot send notifications", message.getId());
            return;
        }


        String title;
        List<String> members;

        Optional<Group> groupWrapper = groupRepository.findById(message.getGroupId());
        if (groupWrapper.isEmpty()) {
            Optional<Channel> channelWrapper = channelRepository.findById(message.getGroupId());
            if (channelWrapper.isEmpty()) {
                return;
            }

            Channel channel = channelWrapper.get();
            Group parentGroup = groupRepository.findById(channel.getParentId()).orElse(null);
            title = ChannelType.PRIVATE_MESSAGE.equals(channel.getType())
                    ? parentGroup.getName() + "\n" + message.getSender().getName()
                    : channel.getName();
            members = channel.getUserIds().stream()
                    .filter(id -> !id.equals(message.getSender().getId()))
                    .distinct()
                    .toList();
        } else {
            Group group = groupWrapper.get();
            title = group.getName();
            members = Stream.concat(group.getMentors().stream(), group.getMentees().stream())
                    .filter(id -> !id.equals(message.getSender().getId()))
                    .distinct()
                    .toList();
        }

        if (members.isEmpty()) {
            return;
        }

        NotificationType type;
        StringBuilder notificationBody = new StringBuilder((message.getSender() != null) ? (message.getSender().getName() + " đã gửi ") : "");
        if (Message.Type.IMAGE.equals(message.getType())) {
            type = NEW_IMAGE_MESSAGE;
            notificationBody.append(message.getImages().size()).append(" ảnh mới.");
        } else if (Message.Type.FILE.equals(message.getType())) {
            type = NEW_FILE_MESSAGE;
            notificationBody.append(" một tệp đính kèm mới.");
        } else {
            type = SYSTEM;
            notificationBody.append(" một đa phương tiện mới.");
        }

        String body = notificationBody.toString();
        try {
            firebaseMessagingManager.sendGroupNotification(members, title, body, attachDataNotification(message.getGroupId(), type));
        } catch (FirebaseMessagingException e) {
            logger.error("Error sending media message notification", e);
        }
    }

    @Override
    public Notification createNewMediaNotification(
            String title, String content, String senderId, Group group) {
        List<String> receiverIds =
                Stream.concat(group.getMentors().stream(), group.getMentees().stream())
                        .filter(id -> !id.equals(senderId))
                        .distinct()
                        .toList();
        Notification notif =
                Notification.builder()
                        .title(title)
                        .content(content)
                        .type(NEW_FILE_MESSAGE)
                        .senderId(senderId)
                        .refId(group.getId())
                        .receiverIds(receiverIds)
                        .build();
        return notificationRepository.save(notif);
    }

    @Override
    @Async
    public void sendNewReactNotification(
            Message message, ReactMessageResponse reaction, String senderId) {
        if (message == null || reaction == null) {
            return;
        }

        if (message.getSenderId() == null || senderId == null) {
            return;
        }

        if (senderId.equals(message.getSenderId())) {
            return;
        }

        Optional<Group> groupWrapper = groupRepository.findById(message.getGroupId());
        if (!groupWrapper.isPresent()) {
            return;
        }
        Group group = groupWrapper.get();

        Map<String, String> data = attachDataNotification(message.getGroupId(), NEW_REACTION);
        com.google.firebase.messaging.Notification notification =
                com.google.firebase.messaging.Notification.builder()
                        .setTitle(group.getName())
                        .setBody(reaction.getName() + " đã thể hiện cảm xúc tin nhắn của bạn.")
                        .build();
        try {
            Optional<NotificationSubscriber> wrapper =
                    notificationSubscriberRepository.findByUserId(message.getSenderId());
            if (!wrapper.isPresent()) {
                return;
            }
            NotificationSubscriber subscriber = wrapper.get();
            firebaseMessagingManager.sendNotification(subscriber.getToken(), notification, data);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    @Override
    @Async
    public void sendRescheduleMeetingNotification(
            String modifierId, Meeting meeting, RescheduleMeetingRequest request) {
        if (meeting == null) {
            return;
        }

        Optional<Group> groupWrapper = groupRepository.findById(meeting.getGroupId());
        if (!groupWrapper.isPresent()) {
            return;
        }
        Group group = groupWrapper.get();

        String title = group.getName();
        String content = "Lịch hẹn: \"" + meeting.getTitle() + "\" đã được dời thời gian.";
        Notification notif = createRescheduleMeetingNotification(title, content, modifierId, group, meeting);
        try {
            firebaseMessagingManager.sendGroupNotification(
                    notif.getReceiverIds(),
                    title,
                    content,
                    attachDataNotification(meeting.getGroupId(), RESCHEDULE_MEETING));
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Notification createRescheduleMeetingNotification(
            String title, String content, String senderId, Group group, Meeting meeting) {
        List<String> receiverIds =
                Stream.concat(group.getMentors().stream(), group.getMentees().stream())
                        .filter(id -> !id.equals(senderId))
                        .distinct()
                        .toList();
        Notification notif =
                Notification.builder()
                        .title(title)
                        .content(content)
                        .type(RESCHEDULE_MEETING)
                        .senderId(senderId)
                        .refId(meeting.getId())
                        .receiverIds(receiverIds)
                        .build();
        return notificationRepository.save(notif);
    }

    @Override
    public long getUnreadNumber(String userId) {
        return notificationRepository.countDistinctByReceiverIdsIn(Collections.singletonList(userId));
    }

    @Override
    @Async
    public void sendNewVoteNotification(String creatorId, Vote vote) {
        if (vote == null) {
            return;
        }

        Optional<Group> groupWrapper = groupRepository.findById(vote.getGroupId());
        if (!groupWrapper.isPresent()) {
            return;
        }
        Group group = groupWrapper.get();

        String title = group.getName();
        String content = "Nhóm có cuộc bình chọn mới \"" + vote.getQuestion() + "\"";
        Notification notif = createNewVoteNotification(title, content, creatorId, group, vote);
        try {
            firebaseMessagingManager.sendGroupNotification(
                    notif.getReceiverIds(),
                    title,
                    content,
                    attachDataNotification(vote.getGroupId(), NEW_VOTE));
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Notification createNewVoteNotification(
            String title, String content, String senderId, Group group, Vote vote) {
        List<String> receiverIds =
                Stream.concat(group.getMentors().stream(), group.getMentees().stream())
                        .filter(id -> !id.equals(senderId))
                        .distinct()
                        .toList();
        Notification notif =
                Notification.builder()
                        .title(title)
                        .content(content)
                        .type(NEW_VOTE)
                        .senderId(senderId)
                        .refId(vote.getId())
                        .receiverIds(receiverIds)
                        .build();
        return notificationRepository.save(notif);
    }

    @Override
    public void sendNewPinNotification(MessageDetailResponse message, User pinner) {
        if (message == null) {
            return;
        }

        if (pinner == null) {
            return;
        }

        String title;
        List<String> members;

        Optional<Group> groupWrapper = groupRepository.findById(message.getGroupId());
        if (groupWrapper.isEmpty()) {
            Optional<Channel> channelWrapper = channelRepository.findById(message.getGroupId());
            if (channelWrapper.isEmpty()) {
                return;
            }

            Channel channel = channelWrapper.get();
            Group parentGroup = groupRepository.findById(channel.getParentId()).orElse(null);
            title = ChannelType.PRIVATE_MESSAGE.equals(channel.getType())
                    ? parentGroup.getName() + "\n" + message.getSender().getName()
                    : channel.getName();
            members = channel.getUserIds().stream()
                    .filter(id -> !id.equals(message.getSender().getId()))
                    .distinct()
                    .toList();
        } else {
            Group group = groupWrapper.get();
            title = group.getName();
            members =
                    Stream.concat(group.getMentors().stream(), group.getMentees().stream())
                            .filter(id -> !id.equals(message.getSender().getId()))
                            .distinct()
                            .toList();
        }

        if (members.isEmpty()) {
            return;
        }

        Map<String, String> data = attachDataNotification(message.getGroupId(), PIN_MESSAGE);
        String shortMessage =
                Jsoup.parse(message.getContent().substring(0, Math.min(message.getContent().length(), 25)))
                        .text();
        String content = pinner.getName() + " đã ghim tin nhắn \"" + shortMessage + "\"";
        try {
            firebaseMessagingManager.sendGroupNotification(members, title, content, data);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendNewUnpinNotification(MessageDetailResponse message, User pinner) {
        if (message == null) {
            return;
        }

        if (pinner == null) {
            return;
        }

        String title;
        List<String> members;

        Optional<Group> groupWrapper = groupRepository.findById(message.getGroupId());
        if (!groupWrapper.isPresent()) {
            Optional<Channel> channelWrapper = channelRepository.findById(message.getGroupId());
            if (!channelWrapper.isPresent()) {
                return;
            }

            Channel channel = channelWrapper.get();
            Group parentGroup = groupRepository.findById(channel.getParentId()).orElse(null);
            title =
                    ChannelType.PRIVATE_MESSAGE.equals(channel.getType())
                            ? parentGroup.getName() + "\n" + message.getSender().getName()
                            : channel.getName();
            members =
                    channel.getUserIds().stream()
                            .filter(id -> !id.equals(message.getSender().getId()))
                            .distinct()
                            .toList();
        } else {
            Group group = groupWrapper.get();
            title = group.getName();
            members =
                    Stream.concat(group.getMentors().stream(), group.getMentees().stream())
                            .filter(id -> !id.equals(message.getSender().getId()))
                            .distinct()
                            .toList();
        }

        if (members.isEmpty()) {
            return;
        }

        Map<String, String> data = attachDataNotification(message.getGroupId(), UNPIN_MESSAGE);
        String shortMessage =
                Jsoup.parse(message.getContent().substring(0, Math.min(message.getContent().length(), 25)))
                        .text();
        String content = pinner.getName() + " đã bỏ ghim tin nhắn \"" + shortMessage + "\"";
        try {
            firebaseMessagingManager.sendGroupNotification(members, title, content, data);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }
}
