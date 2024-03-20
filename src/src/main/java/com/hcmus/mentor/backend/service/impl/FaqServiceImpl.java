package com.hcmus.mentor.backend.service.impl;

import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
import com.hcmus.mentor.backend.controller.payload.request.faqs.CreateFaqRequest;
import com.hcmus.mentor.backend.controller.payload.request.faqs.ImportFAQsRequest;
import com.hcmus.mentor.backend.controller.payload.request.faqs.UpdateFaqRequest;
import com.hcmus.mentor.backend.controller.payload.response.FAQDetail;
import com.hcmus.mentor.backend.controller.payload.response.groups.GroupDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.domain.Faq;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.repository.FaqRepository;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.userdetails.CustomerUserDetails;
import com.hcmus.mentor.backend.service.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FaqServiceImpl implements FaqService {

    private final FaqRepository faqRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    @Override
    public List<Faq> getByGroupId(String userId, String groupId) {
        var group = groupRepository.findById(groupId).orElse(null);
        if (group == null || !group.isMember(userId)) {
            return Collections.emptyList();
        }

        return faqRepository.findByGroupId(groupId).stream()
                .sorted(Comparator.comparing(Faq::getRating).reversed())
                .toList();
    }

    @Override
    public FAQDetail getById(String userId, String faqId) {
        var faq = faqRepository.findById(faqId).orElse(null);
        if (faq == null) {
            return null;
        }

        List<GroupDetailResponse> groupWrapper = groupRepository.getGroupDetail(faq.getGroup().getId());
        if (groupWrapper.size() == 0) {
            return null;
        }
        GroupDetailResponse group = groupWrapper.get(0);
        group.setRole(userId);
        ShortProfile creator = userRepository.findShortProfile(faq.getCreator().getId());
        return FAQDetail.from(faq, creator, group);
    }

    @Override
    public Faq createFaq(String userId, CreateFaqRequest request) {
        Group group = groupRepository.findById(request.getGroupId()).orElseThrow(() -> new DomainException("Không tìm thấy nhóm với id " + request.getGroupId()));

        if (!group.isMentor(userId)) {
            throw new ForbiddenException("Chỉ có mentor được tạo câu hỏi mới");
        }

        Faq faq = faqRepository.save(Faq.builder()
                .question(request.getQuestion())
                .answer(request.getAnswer())
                .creator(userRepository.findById(userId).orElse(null))
                .group(groupRepository.findById(request.getGroupId()).orElse(null))
                .build());

        if (!group.getFaqIds().contains(faq)) {
            group.getFaqIds().add(faq);
        }
        groupRepository.save(group);

        return faq;
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
    public void deleteFaq(String deleter, String faqId) {
        Faq faq = faqRepository.findById(faqId).orElseThrow(() -> new DomainException("Không tìm thấy câu hỏi với id " + faqId));

        var group = faq.getGroup();
        if (!group.isMentor(deleter)) {
            throw new ForbiddenException("Chỉ có mentor được xoá câu hỏi");
        }

        if (group.getFaqIds().contains(faqId)) {
            group.getFaqIds().remove(faq);

            groupRepository.save(group);
        }

        faqRepository.deleteById(faqId);
    }

    @Override
    public void importFaqs(String creatorId, String toGroupId, ImportFAQsRequest request) {
        Group group = groupRepository.findById(toGroupId).orElseThrow(() -> new DomainException("Không tìm thấy nhóm với id " + toGroupId));

        if (!group.isMentor(creatorId)) {
            throw new ForbiddenException("Chỉ có mentor được tạo câu hỏi mới");
        }

        var creator = userRepository.findById(creatorId).orElse(null);
        List<Faq> faqs = faqRepository.findByIdIn(request.getFaqIds()).stream()
                .map(faq -> Faq.builder()
                        .question(faq.getQuestion())
                        .answer(faq.getAnswer())
                        .creator(creator)
                        .group(group)
                        .build())
                .toList();

        faqRepository.saveAll(faqs);

        var groupFaq = group.getFaqIds();
        for (Faq faq : faqs) {
            if (!groupFaq.contains(faq)) {
                groupFaq.add(faq);
            }
        }

        groupRepository.save(group);
    }

    @Override
    public boolean upvote(CustomerUserDetails userDetails, String faqId) {
        var faq = faqRepository.findById(faqId).orElse(null);
        if (faq == null) {
            return false;
        }

        var user = userRepository.findById(userDetails.getId()).orElse(null);
        if (user == null) {
            return false;
        }

        if (!faq.getGroup().isMember(user.getId())) {
            return false;
        }

        faq.addVoter(user);
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

        Optional<Group> groupWrapper = groupRepository.findById(faq.getGroup().getId());
        if (!groupWrapper.isPresent()) {
            return false;
        }
        Group group = groupWrapper.get();
        if (!group.isMember(user.getId())) {
            return false;
        }

        faq.removeVote(userRepository.findById(user.getId()).orElse(null));
        faqRepository.save(faq);
        return true;
    }
}
