package com.hcmus.mentor.backend.service.impl;

import com.hcmus.mentor.backend.domain.Faq;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.controller.payload.request.faqs.CreateFaqRequest;
import com.hcmus.mentor.backend.controller.payload.request.faqs.ImportFAQsRequest;
import com.hcmus.mentor.backend.controller.payload.request.faqs.UpdateFaqRequest;
import com.hcmus.mentor.backend.controller.payload.response.FAQDetail;
import com.hcmus.mentor.backend.controller.payload.response.groups.GroupDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.repository.FaqRepository;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.service.FaqService;
import com.hcmus.mentor.backend.service.PermissionService;
import com.hcmus.mentor.backend.security.principal.userdetails.CustomerUserDetails;

import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FaqServiceImpl implements FaqService {

    private final FaqRepository faqRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;

    @Override
    public List<Faq> getByGroupId(String userId, String groupId) {
        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        if (!groupWrapper.isPresent()) {
            return Collections.emptyList();
        }
        Group group = groupWrapper.get();
        if (!group.isMember(userId)) {
            return Collections.emptyList();
        }

        return faqRepository.findByGroupId(groupId).stream()
                .sorted(Comparator.comparing(Faq::getRating).reversed())
                .toList();
    }

    @Override
    public FAQDetail getById(String userId, String faqId) {
        Optional<Faq> faqWrapper = faqRepository.findById(faqId);
        if (!faqWrapper.isPresent()) {
            return null;
        }
        Faq faq = faqWrapper.get();

        List<GroupDetailResponse> groupWrapper = groupRepository.getGroupDetail(faq.getGroupId());
        if (groupWrapper.size() == 0) {
            return null;
        }
        GroupDetailResponse group = groupWrapper.get(0);
        group.setRole(userId);
        ShortProfile creator = userRepository.findShortProfile(faq.getCreatorId());
        return FAQDetail.from(faq, creator, group);
    }

    @Override
    public Faq addNewFaq(String userId, CreateFaqRequest request) {
        Optional<Group> groupWrapper = groupRepository.findById(request.getGroupId());
        if (!groupWrapper.isPresent()) {
            return null;
        }
        Group group = groupWrapper.get();
        if (!group.isMentor(userId)) {
            return null;
        }

        Faq data =
                Faq.builder()
                        .question(request.getQuestion())
                        .answer(request.getAnswer())
                        .creatorId(userId)
                        .groupId(request.getGroupId())
                        .build();
        Faq newFAQ = faqRepository.save(data);
        if (newFAQ == null) {
            return null;
        }

        group.addFaq(newFAQ.getId());
        groupRepository.save(group);
        return newFAQ;
    }

    @Override
    public Faq updateFAQ(String userId, String faqId, UpdateFaqRequest request) {
        Optional<Group> groupWrapper = groupRepository.findById(request.getGroupId());
        if (!groupWrapper.isPresent()) {
            return null;
        }
        Group group = groupWrapper.get();
        if (!group.isMentor(userId)) {
            return null;
        }

        Optional<Faq> faqWrapper = faqRepository.findById(faqId);
        if (!faqWrapper.isPresent()) {
            return null;
        }
        Faq faq = faqWrapper.get();
        faq.update(request);
        return faqRepository.save(faq);
    }

    @Override
    public boolean deleteFaq(String userId, String faqId) {
        Optional<Faq> faqWrapper = faqRepository.findById(faqId);
        if (!faqWrapper.isPresent()) {
            return false;
        }
        Faq faq = faqWrapper.get();

        Optional<Group> groupWrapper = groupRepository.findById(faq.getGroupId());
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

    @Override
    public void importFAQs(CustomerUserDetails user, String toGroupId, ImportFAQsRequest request) {
        if (!permissionService.isMentor(user.getEmail(), toGroupId)) {
            return;
        }

        Optional<Group> groupWrapper = groupRepository.findById(toGroupId);
        if (!groupWrapper.isPresent()) {
            return;
        }

        List<Faq> newFAQs =
                faqRepository.findByIdIn(request.getFaqIds()).stream()
                        .map(
                                faq ->
                                        Faq.builder()
                                                .question(faq.getQuestion())
                                                .answer(faq.getAnswer())
                                                .creatorId(user.getId())
                                                .groupId(toGroupId)
                                                .build())
                        .toList();

        Group group = groupWrapper.get();
        group.importFaq(newFAQs.stream().map(Faq::getId).toList());
        groupRepository.save(group);
        faqRepository.saveAll(newFAQs);
    }

    @Override
    public boolean upvote(CustomerUserDetails user, String faqId) {
        Optional<Faq> faqWrapper = faqRepository.findById(faqId);
        if (!faqWrapper.isPresent()) {
            return false;
        }
        Faq faq = faqWrapper.get();

        Optional<Group> groupWrapper = groupRepository.findById(faq.getGroupId());
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

    @Override
    public boolean downVote(CustomerUserDetails user, String faqId) {
        Optional<Faq> faqWrapper = faqRepository.findById(faqId);
        if (!faqWrapper.isPresent()) {
            return false;
        }
        Faq faq = faqWrapper.get();

        Optional<Group> groupWrapper = groupRepository.findById(faq.getGroupId());
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
