package com.hcmus.mentor.backend.service.impl;

import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.payload.request.meetings.RescheduleMeetingRequest;
import com.hcmus.mentor.backend.controller.payload.request.notifications.AddNotificationRequest;
import com.hcmus.mentor.backend.controller.payload.request.notifications.SubscribeNotificationServerRequest;
import com.hcmus.mentor.backend.controller.payload.response.NotificationResponse;
import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.controller.usecase.notification.common.NotificationDetailDto;
import com.hcmus.mentor.backend.domain.*;
import com.hcmus.mentor.backend.domain.constant.NotificationAction;
import com.hcmus.mentor.backend.domain.constant.NotificationType;
import com.hcmus.mentor.backend.repository.NotificationRepository;
import com.hcmus.mentor.backend.repository.NotificationSubscriberRepository;
import com.hcmus.mentor.backend.repository.NotificationUserRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.modelmapper.ModelMapper;
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
    private final LoggedUserAccessor loggedUserAccessor;
    private final NotificationRepository notificationRepository;
    private final NotificationSubscriberRepository notificationSubscriberRepository;
    private final FirebaseServiceImpl firebaseService;
    private final UserRepository userRepository;
    private final NotificationUserRepository notificationUserRepository;
    private final ModelMapper modelMapper;

    private static final String NOT_FOUND_NOTIFICATION_MESSAGE = "Không tìm thấy thông báo.";
    private static final String NOT_FOUND_USER_MESSAGE = "Không tìm thấy nguời.";

    @Override
    public NotificationDetailDto getById(String notificationId) {
        var notification = notificationRepository.findById(notificationId).orElseThrow(() -> new DomainException(NOT_FOUND_NOTIFICATION_MESSAGE));

        return modelMapper.map(notification, NotificationDetailDto.class);
    }

    @Override
    public Map<String, Object> getOwn(int page, int size) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();
        PageRequest paging = PageRequest.of(page, size, Sort.by("createdDate").descending());

        Page<Notification> notifications = notificationRepository.findOwnNotifications(Collections.singletonList(currentUserId), paging);
        List<NotificationResponse> notificationsResponse = notifications.stream()
                .map(notification -> {
                    var response = modelMapper.map(notification, NotificationResponse.class);
                    var shortProfile = userRepository.findById(notification.getSender().getId())
                            .map(u -> modelMapper.map(u, ShortProfile.class))
                            .orElse(null);
                    response.setSender(shortProfile);

                    return response;
                })
                .toList();

        return pagingResponse(notifications, notificationsResponse);
    }

    /**
     * @param request Request to create a notification
     * @return Notification
     */
    @Override
    public Notification create(AddNotificationRequest request) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        Notification notification = notificationRepository.save(Notification.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .type(NotificationType.NEED_RESPONSE)
                .sender(userRepository.findById(currentUserId).orElse(null))
                .createdDate(request.getCreatedDate())
                .build());

        var receivers = NotificationUser.builder().notification(notification).user(userRepository.findById(request.getReceiverId()).orElse(null)).build();
        notification.setReceivers(Collections.singletonList(receivers));

        notification = notificationRepository.save(notification);

        return notification;
    }

    /**
     * @param notificationId ID of the notification
     * @param action         Action to response the notification
     * @return Notification
     */
    @Override
    public Notification response(String notificationId, NotificationAction action) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(() -> new DomainException("Notification not found"));

        if (!notification.getType().equals(NotificationType.NEED_RESPONSE)) {
            throw new DomainException("Notification is not a response type");
        }

        NotificationUser notificationUser = notification.getReceivers().stream()
                .filter(nu -> nu.getUser().getId().equals(currentUserId))
                .findFirst()
                .orElseThrow(() -> new DomainException("User is not a receiver of this notification"));

        switch (action) {
            case SEEN:
                notificationUser.setIsReaded(true);
                break;
            case ACCEPT:
                notificationUser.setIsReaded(true);
                notificationUser.setIsAgreed(true);
                break;
            case REFUSE:
                notificationUser.setIsReaded(true);
                notificationUser.setIsAgreed(false);
                break;
            default:
                break;
        }
        notificationUserRepository.save(notificationUser);

        return notification;
    }

    @Override
    public void subscribeToServer(SubscribeNotificationServerRequest request) {
        var user = userRepository.findById(request.getUserId()).orElseThrow(() -> new DomainException(NOT_FOUND_USER_MESSAGE));

        notificationSubscriberRepository.deleteByUserId(request.getUserId());

        logger.info("Unsubscribe user notification with userId {}", request.getUserId());

        List<NotificationSubscriber> subscribes = notificationSubscriberRepository.findByUserIdOrToken(request.getUserId(), request.getToken());
        if (subscribes.isEmpty()) {
            NotificationSubscriber subscriber = NotificationSubscriber.builder()
                    .user(user)
                    .token(request.getToken())
                    .build();
            notificationSubscriberRepository.save(subscriber);
            return;
        }

        subscribes.forEach(subscriber -> {
            subscriber.setToken(request.getToken());
            subscriber.setUser(user);
        });

        logger.info("Subscribe user notification with userId {}, token {}", request.getUserId(), request.getToken());

        notificationSubscriberRepository.saveAll(subscribes);
    }

    @Override
    public void unsubscribeNotification(String userId) {
        notificationSubscriberRepository.deleteByUserId(userId);
    }

    @Override
    public long getCountUnread() {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        return notificationUserRepository.countByUserIdAndIsDeletedIsFalse(currentUserId);
    }

    @Override
    public void sendForMessage(Message message) {
        if (message == null) {
            return;
        }

        var sender = message.getSender();
        var title = getTitleForChannel(message.getChannel(), sender);
        var body = String.format("%s: %s", sender.getName(), Jsoup.parse(message.getContent()).text());
        var notification = createNotification(title, body, NEW_MESSAGE, sender, message.getChannel().getUsers(), message.getId());
        var data = attachDataNotification(message.getChannel().getId(), NEW_MESSAGE);
        data.put("sender", sender.getName());
        data.put("imageUrl", sender.getImageUrl());

        var receiver = notification.getReceivers().stream().map(NotificationUser::getUser).map(User::getId).toList();

        firebaseService.sendNotificationMulticast(receiver, title, body, data);
    }


    @Override
    public void sendForTask(Task task) {
        if (task == null) {
            return;
        }

        var title = getTitleForChannel(task.getGroup(), task.getAssigner());
        var body = String.format("Nhóm có công việc mới \"%s\"", task.getTitle());
        var notification = createNotification(title, body, NEW_TASK, task.getAssigner(), task.getAssignees().stream().map(Assignee::getUser).toList(), task.getId());
        var receivers = notification.getReceivers().stream().map(n -> n.getUser().getId()).toList();
        var data = attachDataNotification(task.getGroup().getId(), NEW_TASK);

        firebaseService.sendNotificationMulticast(receivers, title, body, data);
    }

    @Override
    public void sendForMeeting(Meeting meeting) {
        if (meeting == null || meeting.getGroup() == null || meeting.getGroup().getGroup() == null) {
            return;
        }

        var title = getTitleForChannel(meeting.getGroup(), meeting.getOrganizer());
        var body = String.format("Nhóm có lịch hẹn mới \"%s\"", meeting.getTitle());
        var notification = createNotification(title, body, NEW_MEETING, meeting.getOrganizer(), meeting.getAttendees(), meeting.getId());
        var receiverIds = notification.getReceivers().stream().map(nu -> nu.getUser().getId()).toList();
        var data = attachDataNotification(meeting.getGroup().getId(), NEW_MEETING);

        firebaseService.sendNotificationMulticast(receiverIds, title, body, data);
    }

    @Override
    public void sendForMediaMessage(Message message) {
        boolean isImageMessage = Message.Type.IMAGE.equals(message.getType()) && message.getImages() != null && !message.getImages().isEmpty();
        boolean isFileMessage = Message.Type.FILE.equals(message.getType()) && message.getFile() != null;
        if (!isImageMessage && !isFileMessage) {
            return;
        }

        NotificationType type;
        StringBuilder notificationBody = new StringBuilder(String.format("%s đã gửi ", message.getSender().getName()));
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
    public void sendForRescheduleMeeting(User modifier, Meeting meeting, RescheduleMeetingRequest request) {
        if (meeting == null || meeting.getGroup() == null || meeting.getGroup().getGroup() == null) {
            return;
        }

        var title = getTitleForChannel(meeting.getGroup(), modifier);
        var body = String.format("Lịch hẹn: \"%s\" đã được dời thời gian.", meeting.getTitle());
        var receivers = Stream.concat(Stream.of(meeting.getOrganizer()), meeting.getAttendees().stream()).toList();
        var notification = createNotification(title, body, RESCHEDULE_MEETING, modifier, receivers, meeting.getId());
        var receiverIds = notification.getReceivers().stream().map(nu -> nu.getUser().getId()).toList();
        var data = attachDataNotification(meeting.getGroup().getId(), RESCHEDULE_MEETING);

        firebaseService.sendNotificationMulticast(receiverIds, title, body, data);
    }


    @Override
    public void sendForNote(String creatorId, Vote vote) {
        Optional.of(vote).ifPresent(v -> {
            var title = getTitleForChannel(v.getGroup(), v.getCreator());
            var body = String.format("Nhóm có cuộc bình chọn mới \"%s\"", v.getQuestion());
            var notification = createNotification(title, body, NEW_VOTE, v.getCreator(), v.getGroup().getUsers(), v.getId());
            var receiverIds = notification.getReceivers().stream().map(nu -> nu.getUser().getId()).toList();
            var data = attachDataNotification(v.getGroup().getId(), NEW_VOTE);

            firebaseService.sendNotificationMulticast(receiverIds, title, body, data);
        });
    }

    @Override
    public void sendForTogglePin(Message message, User pinner, boolean isPin) {
        if (message == null || pinner == null) {
            return;
        }

        String prefixBody = String.format("%s đã %sghim tin nhắn \"", pinner.getName(), isPin ? "" : "bỏ ");
        sendAboutMessageNotification(message, pinner, isPin ? PIN_MESSAGE : UNPIN_MESSAGE, prefixBody, null, message.getId());
    }

    @Override
    @SneakyThrows
    public void sendForForwardMessage(List<Message> messages, User sender) {
        if (messages == null || sender == null || messages.isEmpty()) {
            return;
        }

        messages.forEach(message -> {
                    var body = sender.getName() + " đã chuyển tiếp tin nhắn \"";
                    sendAboutMessageNotification(message, sender, FORWARD, body, null, message.getId());
                }
        );

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

    private void sendAboutMessageNotification(Message message, User sender, NotificationType type, String prefixBody, String body, String refId) {
        if (message == null || sender == null) {
            return;
        }

        var channel = message.getChannel();
        var title = getTitleForChannel(channel, sender);

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

    private String getTitleForChannel(Channel channel, User sender) {
        if (channel == null || sender == null) {
            return "";
        }

        var groupName = Optional.of(channel).map(Channel::getGroup).map(Group::getName).orElse("");
        if (channel.getType() == PRIVATE_MESSAGE) {
            return String.format("%s %n %s", groupName, sender.getName());
        } else {
            return String.format("%s - %s", groupName, channel.getName());
        }
    }

    private Map<String, String> attachDataNotification(String groupId, NotificationType type) {
        Map<String, String> data = new HashMap<>();
        data.put("type", type.name());
        data.put("screen", "chat");
        data.put("groupId", groupId);
        return data;
    }

    private Map<String, Object> pagingResponse(Slice<Notification> slice, List<NotificationResponse> notifications) {
        Map<String, Object> response = new HashMap<>();
        response.put("notifications", notifications);
        response.put("hasMore", slice.hasNext());
        return response;
    }
}