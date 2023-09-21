package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.entity.FAQ;
import com.hcmus.mentor.backend.entity.Group;
import com.hcmus.mentor.backend.manager.GoogleDriveManager;
import com.hcmus.mentor.backend.payload.request.faqs.CreateFaqRequest;
import com.hcmus.mentor.backend.payload.request.faqs.ImportFAQsRequest;
import com.hcmus.mentor.backend.payload.request.faqs.UpdateFaqRequest;
import com.hcmus.mentor.backend.payload.response.FAQDetail;
import com.hcmus.mentor.backend.payload.response.groups.GroupDetailResponse;
import com.hcmus.mentor.backend.payload.response.groups.ShortGroup;
import com.hcmus.mentor.backend.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.repository.*;
import com.hcmus.mentor.backend.security.UserPrincipal;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FaqService {

    private final FaqRepository faqRepository;

    private final GroupRepository groupRepository;

    private final GroupCategoryRepository groupCategoryRepository;

    private final UserRepository userRepository;

    private final UserService userService;

    private final MailService mailService;

    private final PermissionService permissionService;

    private final MongoTemplate mongoTemplate;

    private final SystemConfigRepository systemConfigRepository;

    private final GoogleDriveManager googleDriveManager;

    private final MessageRepository messageRepository;

    private final MessageService messageService;

    private final SocketIOService socketIOService;

    private final NotificationService notificationService;

    public FaqService(FaqRepository faqRepository, GroupRepository groupRepository,
                      GroupCategoryRepository groupCategoryRepository,
                      UserRepository userRepository,
                      UserService userService,
                      MailService mailService,
                      PermissionService permissionService,
                      MongoTemplate mongoTemplate,
                      SystemConfigRepository systemConfigRepository,
                      GoogleDriveManager googleDriveManager,
                      MessageRepository messageRepository,
                      MessageService messageService,
                      SocketIOService socketIOService,
                      NotificationService notificationService) {
        this.faqRepository = faqRepository;
        this.groupRepository = groupRepository;
        this.groupCategoryRepository = groupCategoryRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.mailService = mailService;
        this.permissionService = permissionService;
        this.mongoTemplate = mongoTemplate;
        this.systemConfigRepository = systemConfigRepository;
        this.googleDriveManager = googleDriveManager;
        this.messageRepository = messageRepository;
        this.messageService = messageService;
        this.socketIOService = socketIOService;
        this.notificationService = notificationService;
    }

    public List<FAQ> getByGroupId(String userId, String groupId) {
        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        if (!groupWrapper.isPresent()) {
            return Collections.emptyList();
        }
        Group group = groupWrapper.get();
        if (!group.isMember(userId)) {
            return Collections.emptyList();
        }

        return faqRepository.findByGroupId(groupId)
                .stream()
                .sorted(Comparator.comparing(FAQ::getRating).reversed())
                .collect(Collectors.toList());
    }

    public FAQDetail getById(String userId, String faqId) {
        Optional<FAQ> faqWrapper = faqRepository.findById(faqId);
        if (!faqWrapper.isPresent()) {
            return null;
        }
        FAQ faq = faqWrapper.get();

        List<GroupDetailResponse> groupWrapper = groupRepository.getGroupDetail(faq.getGroupId());
        if (groupWrapper.size() == 0) {
            return null;
        }
        GroupDetailResponse group = groupWrapper.get(0);
        group.setRole(userId);
        ShortProfile creator = userRepository.findShortProfile(faq.getCreatorId());
        return FAQDetail.from(faq, creator, group);
    }

    public FAQ addNewFaq(String userId, CreateFaqRequest request) {
        Optional<Group> groupWrapper = groupRepository
                .findById(request.getGroupId());
        if (!groupWrapper.isPresent()) {
            return null;
        }
        Group group = groupWrapper.get();
        if (!group.isMentor(userId)) {
            return null;
        }

        FAQ data = FAQ.builder()
                .question(request.getQuestion())
                .answer(request.getAnswer())
                .creatorId(userId)
                .groupId(request.getGroupId())
                .build();
        FAQ newFAQ = faqRepository.save(data);
        if (newFAQ == null) {
            return null;
        }

        group.addFaq(newFAQ.getId());
        groupRepository.save(group);
        return newFAQ;
    }

    public FAQ updateFAQ(String userId, String faqId, UpdateFaqRequest request) {
        Optional<Group> groupWrapper = groupRepository
                .findById(request.getGroupId());
        if (!groupWrapper.isPresent()) {
            return null;
        }
        Group group = groupWrapper.get();
        if (!group.isMentor(userId)) {
            return null;
        }

        Optional<FAQ> faqWrapper = faqRepository.findById(faqId);
        if (!faqWrapper.isPresent()) {
            return null;
        }
        FAQ faq = faqWrapper.get();
        faq.update(request);
        return faqRepository.save(faq);
    }

    public boolean deleteFaq(String userId, String faqId) {
        Optional<FAQ> faqWrapper = faqRepository.findById(faqId);
        if (!faqWrapper.isPresent()) {
            return false;
        }
        FAQ faq = faqWrapper.get();

        Optional<Group> groupWrapper = groupRepository
                .findById(faq.getGroupId());
        if (!groupWrapper.isPresent()) {
            return false;
        }
        Group group = groupWrapper.get();
        if (!group.isMentor(userId)) {
            return false;
        }
        group.deleteFaq(faqId);
        groupRepository.save(group);
        faqRepository.deleteById(faqId);
        return true;
    }

    public void importFAQs(UserPrincipal user, String toGroupId, ImportFAQsRequest request) {
        if (!permissionService.isMentor(user.getEmail(), toGroupId)) {
            return;
        }

        Optional<Group> groupWrapper = groupRepository
                .findById(toGroupId);
        if (!groupWrapper.isPresent()) {
            return;
        }

        List<FAQ> newFAQs = faqRepository.findByIdIn(request.getFaqIds())
                .stream()
                .map(faq -> FAQ.builder()
                        .question(faq.getQuestion())
                        .answer(faq.getAnswer())
                        .creatorId(user.getId())
                        .groupId(toGroupId)
                        .build())
                .collect(Collectors.toList());

        Group group = groupWrapper.get();
        group.importFaq(newFAQs.stream().map(FAQ::getId)
                .collect(Collectors.toList()));
        groupRepository.save(group);
        faqRepository.saveAll(newFAQs);
    }

    public boolean upvote(UserPrincipal user, String faqId) {
        Optional<FAQ> faqWrapper = faqRepository.findById(faqId);
        if (!faqWrapper.isPresent()) {
            return false;
        }
        FAQ faq = faqWrapper.get();

        Optional<Group> groupWrapper = groupRepository
                .findById(faq.getGroupId());
        if (!groupWrapper.isPresent()) {
            return false;
        }
        Group group = groupWrapper.get();
        if (!group.isMember(user.getId())) {
            return false;
        }

        faq.upvote(user.getId());
        faqRepository.save(faq);
        return true;
    }

    public boolean downVote(UserPrincipal user, String faqId) {
        Optional<FAQ> faqWrapper = faqRepository.findById(faqId);
        if (!faqWrapper.isPresent()) {
            return false;
        }
        FAQ faq = faqWrapper.get();

        Optional<Group> groupWrapper = groupRepository
                .findById(faq.getGroupId());
        if (!groupWrapper.isPresent()) {
            return false;
        }
        Group group = groupWrapper.get();
        if (!group.isMember(user.getId())) {
            return false;
        }

        faq.downVote(user.getId());
        faqRepository.save(faq);
        return true;
    }
}
