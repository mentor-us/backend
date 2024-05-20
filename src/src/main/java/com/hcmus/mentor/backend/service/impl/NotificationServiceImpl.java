package com.hcmus.mentor.backend.service.impl;

import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.payload.request.AddNotificationRequest;
import com.hcmus.mentor.backend.controller.payload.request.RescheduleMeetingRequest;
import com.hcmus.mentor.backend.controller.payload.request.SubscribeNotificationRequest;
import com.hcmus.mentor.backend.controller.payload.response.NotificationResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.ReactMessageResponse;
import com.hcmus.mentor.backend.domain.*;
import com.hcmus.mentor.backend.domain.constant.NotificationType;
import com.hcmus.mentor.backend.event.SendFirebaseNotificationEvent;
import com.hcmus.mentor.backend.repository.*;
import com.hcmus.mentor.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Stream;

import static com.hcmus.mentor.backend.domain.constant.ChannelType.PRIVATE_MESSAGE;
import static com.hcmus.mentor.backend.domain.constant.NotificationType.*;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LogManager.getLogger(NotificationServiceImpl.class);
    private final NotificationRepository notificationRepository;
    private final NotificationSubscriberRepository notificationSubscriberRepository;
    private final GroupRepository groupRepository;
    private final FirebaseServiceImpl firebaseService;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final NotificationUserRepository notificationUserRepository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Map<String, Object> getOwnNotifications(String userId, int page, int size) {
        PageRequest paging = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Notification> notifications = notificationRepository.findOwnNotifications(Collections.singletonList(userId), paging);
        List<NotificationResponse> notificationsResponse = notifications.stream()
                .map(notification -> modelMapper.map(notification, NotificationResponse.class)).toList();
        return pagingResponse(notifications, notificationsResponse);
    }

    /**
     * @param senderId
     * @param request
     * @return
     */
    @Override
    public Notification createResponseNotification(String senderId, AddNotificationRequest request) {
        Notification notif = notificationRepository.save(Notification.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .type(NotificationType.NEED_RESPONSE)
                .sender(userRepository.findById(senderId).orElse(null))
                .createdDate(request.getCreatedDate())
                .build());
        var receivers = NotificationUser.builder().notification(notif).user(userRepository.findById(request.getReceiverId()).orElse(null)).build();
        notif.setReceivers(Collections.singletonList(receivers));
        return notif;
    }


    /**
     * @param userId
     * @param notificationId
     * @param action
     * @return Notification
     */
    @Override
    public Notification responseNotification(String userId, String notificationId, String action) {
        Notification notif = notificationRepository.findById(notificationId).orElseThrow(() -> new NoSuchElementException("Notification not found"));

        if (!notif.getType().equals(NotificationType.NEED_RESPONSE)) {
            throw new DomainException("Notification is not a response type");
        }

        NotificationUser notificationUser = notif.getReceivers().stream()
                .filter(nu -> nu.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new DomainException("User is not a receiver of this notification"));

        switch (action) {
            case "seen":
                notificationUser.setIsReaded(true);
                break;
            case "accept":
                notificationUser.setIsReaded(true);
                notificationUser.setIsAgreed(true);
                break;
            case "refuse":
                notificationUser.setIsReaded(true);
                notificationUser.setIsAgreed(false);
                break;
            default:
                break;
        }
        notificationUserRepository.save(notificationUser);

        return notif;
    }


    /**
     * @param request
     */
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
        logger.info("[*] Subscribe user notification: userID({}) | Token({})", request.getUserId(), request.getToken());

        List<NotificationSubscriber> subscribes =
                notificationSubscriberRepository.findByUserIdOrToken(
                        request.getUserId(), request.getToken());
        if (subscribes.isEmpty()) {
            NotificationSubscriber subscriber =
                    NotificationSubscriber.builder()
                            .user(userRepository.findById(request.getUserId()).orElse(null))
                            .token(request.getToken())
                            .build();
            notificationSubscriberRepository.save(subscriber);
            return;
        }

        subscribes.forEach(subscriber -> {
            subscriber.setToken(request.getToken());
            subscriber.setUser(userRepository.findById(request.getUserId()).orElse(null));
        });
        notificationSubscriberRepository.saveAll(subscribes);
    }

    private Map<String, Object> pagingResponse(Slice<Notification> slice, List<NotificationResponse> notifications) {
        Map<String, Object> response = new HashMap<>();
        response.put("notifications", notifications);
        response.put("hasMore", slice.hasNext());
        return response;
    }

    @Override
    public void unsubscribeNotification(String userId) {
        notificationSubscriberRepository.deleteByUserId(userId);
    }

    @Override
//    @Async
    public void sendNewMessageNotification(Message message) {
        var sender = message.getSender();
        var title = buildTitle(message.getChannel(), sender);
        var body = String.format("%s: %s", sender.getName(), Jsoup.parse(message.getContent()).text());
        var notification = createNotification(title, body, NEW_MESSAGE, sender, message.getChannel().getUsers(), message.getId());
        var data = attachDataNotification(message.getChannel().getId(), NEW_MESSAGE);
        data.put("sender", sender.getName());
        data.put("imageUrl", sender.getImageUrl());
        var receiver = notification.getReceivers().stream().map(NotificationUser::getUser).map(User::getId).toList();
        firebaseService.sendNotificationMulticast(receiver, title, body, data);
    }

    private Map<String, String> attachDataNotification(String groupId, NotificationType type) {
        Map<String, String> data = new HashMap<>();
        data.put("type", type.name());
        data.put("screen", "chat");
        data.put("groupId", groupId);
        return data;
    }

    @Override
    public void sendNewTaskNotification(Task task) {
        if (task == null) {
            return;
        }

        var title = buildTitle(task.getGroup(), task.getAssigner());
        var body = "Nhóm có công việc mới \"" + task.getTitle() + "\"";
        var notification = createNotification(title, body, NEW_TASK, task.getAssigner(), task.getAssignees().stream().map(Assignee::getUser).toList(), task.getId());
        var receivers = notification.getReceivers().stream().map(n -> n.getUser().getId()).toList();
        var data = attachDataNotification(task.getGroup().getId(), NEW_TASK);

        firebaseService.sendNotificationMulticast(receivers, title, body, data);
    }

    @Override
    public void sendNewMeetingNotification(Meeting meeting) {
        if (meeting == null || meeting.getGroup() == null || meeting.getGroup().getGroup() == null) {
            return;
        }

        var title = buildTitle(meeting.getGroup(), meeting.getOrganizer());
        var body = String.format("Nhóm có lịch hẹn mới \"%s\"", meeting.getTitle());
        var notification = createNotification(title, body, NEW_MEETING, meeting.getOrganizer(), meeting.getAttendees(), meeting.getId());
        var receiverIds = notification.getReceivers().stream().map(nu -> nu.getUser().getId()).toList();
        var data = attachDataNotification(meeting.getGroup().getId(), NEW_MEETING);

        firebaseService.sendNotificationMulticast(receiverIds, title, body, data);
    }

    @Override
//    @Async
    public void sendNewMediaMessageNotification(Message message) {
        boolean isImageMessage = Message.Type.IMAGE.equals(message.getType()) && message.getImages() != null && !message.getImages().isEmpty();
        boolean isFileMessage = Message.Type.FILE.equals(message.getType()) && message.getFile() != null;
        if (!isImageMessage && !isFileMessage) {
            return;
        }

        NotificationType type;
        StringBuilder notificationBody = new StringBuilder(message.getSender().getName() + " đã gửi ");
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

        sendAboutMessageNotification(message, message.getSender(), type, null, notificationBody.toString(), message.getId());
    }

    @Override
//    @Async
    public void sendNewReactNotification(Message message, ReactMessageResponse reaction, User sender) {
        if (message == null || reaction == null || message.getSender() == null || sender == null) {
            return;
        }

        NotificationSubscriber subscriber = notificationSubscriberRepository.findByUserId(message.getSender().getId()).orElse(null);
        if (subscriber == null) {
            return;
        }
        var title = buildTitle(message.getChannel(), sender);
        var body = message.getSender().getName() + " đã thể hiện cảm xúc tin nhắn của bạn.";
        Map<String, String> data = attachDataNotification(message.getChannel().getId(), NEW_REACTION);

        var event = new SendFirebaseNotificationEvent(this, Collections.singletonList(subscriber.getToken()), title, body, data);

        applicationEventPublisher.publishEvent(event);
    }

    @Override
//    @Async
    public void sendRescheduleMeetingNotification(User modifier, Meeting meeting, RescheduleMeetingRequest request) {
        if (meeting == null || meeting.getGroup() == null || meeting.getGroup().getGroup() == null) {
            return;
        }

        var title = buildTitle(meeting.getGroup(), modifier);
        var body = "Lịch hẹn: \"" + meeting.getTitle() + "\" đã được dời thời gian.";
        var receivers = Stream.concat(Stream.of(meeting.getOrganizer()), meeting.getAttendees().stream()).toList();
        var notification = createNotification(title, body, RESCHEDULE_MEETING, modifier, receivers, meeting.getId());
        var receiverIds = notification.getReceivers().stream().map(nu -> nu.getUser().getId()).toList();
        var data = attachDataNotification(meeting.getGroup().getId(), RESCHEDULE_MEETING);

        firebaseService.sendNotificationMulticast(receiverIds, title, body, data);
    }


    @Override
    public long getUnreadNumber(final String userId) {
        return 0;
    }

    @Override
    public void sendNewVoteNotification(String creatorId, Vote vote) {
        if (vote == null) {
            return;
        }

        var title = buildTitle(vote.getGroup(), vote.getCreator());
        var body = String.format("Nhóm có cuộc bình chọn mới \"%s\"", vote.getQuestion());
        var notification = createNotification(title, body, NEW_VOTE, vote.getCreator(), vote.getGroup().getUsers(), vote.getId());
        var receiverIds = notification.getReceivers().stream().map(nu -> nu.getUser().getId()).toList();
        var data = attachDataNotification(vote.getGroup().getId(), NEW_VOTE);

        firebaseService.sendNotificationMulticast(receiverIds, title, body, data);
    }

    @Override
    public void sendTogglePinNotification(Message message, User pinner, Boolean isPin) {
        if (message == null || pinner == null) {
            return;
        }

        String prefixBody = pinner.getName() + (isPin ? " đã ghim tin nhắn \"" : " đã bỏ ghim tin nhắn \"");
        sendAboutMessageNotification(message, pinner, isPin ? PIN_MESSAGE : UNPIN_MESSAGE, prefixBody, null, message.getId());
    }

    @Override
    @SneakyThrows
    public void sendForwardMessageNotification(List<Message> messages, User sender) {
        if (messages == null || sender == null || messages.isEmpty()) {
            return;
        }

        messages.forEach(message -> {
                    var body = sender.getName() + " đã chuyển tiếp tin nhắn \"";
                    sendAboutMessageNotification(message, sender, FORWARD, body, null, message.getId());
                }
        );

    }

    private void sendAboutMessageNotification(Message message, User sender, NotificationType type, String prefixBody, String body, String refId) {
        if (message == null || sender == null) {
            return;
        }

        var channel = message.getChannel();
        var title = buildTitle(channel, sender);

        var receivers = channel.getUsers().stream().filter(user -> !user.getId().equals(sender.getId())).toList();
        Map<String, String> data = attachDataNotification(channel.getId(), type);

        if (body == null || body.isEmpty()) {
            String shortMessage = "";
            if (message.getType() == Message.Type.TEXT) {
                shortMessage = Jsoup.parse(message.getContent().substring(0, Math.min(message.getContent().length(), 25)))
                        .text();
            } else {
                switch (message.getType()) {
                    case IMAGE:
                        shortMessage = "ảnh.";
                        break;
                    case FILE:
                        shortMessage = "tệp đính kèm.";
                        break;
                    case VIDEO:
                        shortMessage = "đa phương tiện.";
                        break;
                    case MEETING:
                        shortMessage = "lịch hẹn.";
                        break;
                    case TASK:
                        shortMessage = "công việc.";
                        break;
                    case VOTE:
                        shortMessage = "cuộc bình chọn.";
                        break;
                    case SYSTEM:
                        shortMessage = "thông báo hệ thống.";
                        break;
                    default:
                        break;
                }
            }
            body = prefixBody + shortMessage + "\"";
        }
        createNotification(title, body, type, sender, receivers, refId);

        firebaseService.sendNotificationMulticast(receivers.stream().map(User::getId).toList(), title, body, data);
    }

    private String buildTitle(Channel channel, User sender) {
        if (channel == null || sender == null) {
            return "";
        }

        var group = Optional.of(channel).map(Channel::getGroup).orElse(null);
        String title = Optional.ofNullable(group).map(Group::getName).orElse("");
        if (channel.getType() == PRIVATE_MESSAGE) {
            title = title + "\n" + sender.getName();
        } else {
            title = title + " - " + channel.getName();
        }
        ;
        return title;
    }

    public Notification createNotification(String title, String content, NotificationType type, User sender, List<User> receivers, String refId) {
        Notification notification = Notification.builder()
                .title(title)
                .content(content)
                .type(type)
                .sender(sender)
                .refId(refId)
                .build();

        List<NotificationUser> receiverNotifications = receivers.stream()
                .filter(user -> !user.getId().equals(sender.getId()))
                .map(user -> NotificationUser.builder()
                        .notification(notification)
                        .user(user)
                        .build()
                )
                .toList();
        notification.setReceivers(receiverNotifications);

        return notificationRepository.save(notification);
    }
}