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
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FaqServiceImpl implements FaqService {

    private final FaqRepository faqRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<Faq> getByGroupId(String userId, String groupId) {
        var group = groupRepository.findById(groupId).orElse(null);
        if (group == null || !group.isMember(userId)) {
            return Collections.emptyList();
        }

        return group.getFaqs().stream().sorted(Comparator.comparing(Faq::getRating).reversed()).toList();
    }

    @Override
    public FAQDetail getById(String userId, String faqId) {
        var faq = faqRepository.findById(faqId).orElseThrow(() -> new DomainException("Không tìm thấy câu hỏi với id " + faqId));

        var group = new GroupDetailResponse(faq.getGroup());
        group.setRole(userId);
        ShortProfile creator = modelMapper.map(faq.getCreator(), ShortProfile.class);
        return FAQDetail.from(faq, creator, group);
    }

    @Override
    public Faq createFaq(String userId, CreateFaqRequest request) {
        var user = userRepository.findById(userId).orElseThrow(() -> new DomainException("Không tìm thấy người dùng với id " + userId));
        Group group = groupRepository.findById(request.getGroupId()).orElseThrow(() -> new DomainException("Không tìm thấy nhóm với id " + request.getGroupId()));
        if (!group.isMentor(userId)) {
            throw new ForbiddenException("Chỉ có mentor được tạo câu hỏi mới");
        }

        return faqRepository.save(Faq.builder()
                .question(request.getQuestion())
                .answer(request.getAnswer())
                .creator(user)
                .group(group)
                .build());
    }

    @Override
    public Faq updateFAQ(String userId, String faqId, UpdateFaqRequest request) {
        var faq = faqRepository.findById(faqId).orElseThrow(() -> new DomainException("Không tìm thấy câu hỏi với id " + faqId));

        if (request.getAnswer() != null && !request.getAnswer().isEmpty()) {
            faq.setAnswer(request.getAnswer());
            faq.setUpdatedDate(LocalDateTime.now(ZoneOffset.UTC));
        }
        if (!request.getQuestion().isEmpty()) {
            faq.setQuestion(request.getQuestion());
            faq.setUpdatedDate(LocalDateTime.now(ZoneOffset.UTC));
        }
        return faqRepository.save(faq);
    }

    @Override
    public void deleteFaq(String deleter, String faqId) {
        Faq faq = faqRepository.findById(faqId).orElseThrow(() -> new DomainException("Không tìm thấy câu hỏi với id " + faqId));
        if (!faq.getGroup().isMentor(deleter)) {
            throw new ForbiddenException("Chỉ có mentor được xoá câu hỏi");
        }

        faqRepository.deleteById(faqId);
    }

    @Override
    public void importFaqs(String creatorId, String toGroupId, ImportFAQsRequest request) {
        Group group = groupRepository.findById(toGroupId).orElseThrow(() -> new DomainException("Không tìm thấy nhóm với id " + toGroupId));
        if (!group.isMentor(creatorId)) {
            throw new ForbiddenException("Chỉ có mentor được tạo câu hỏi mới");
        }

        var creator = userRepository.findById(creatorId).orElseThrow(() -> new DomainException("Không tìm thấy người dùng với id " + creatorId));
        List<Faq> faqs = faqRepository.findByIdIn(request.getFaqIds()).stream()
                .map(faq -> Faq.builder()
                        .question(faq.getQuestion())
                        .answer(faq.getAnswer())
                        .creator(creator)
                        .group(group)
                        .build())
                .toList();

        faqRepository.saveAll(faqs);
    }

    @Override
    public void upvote(CustomerUserDetails userDetails, String faqId) {
        var faq = faqRepository.findById(faqId).orElseThrow(() -> new DomainException("Không tìm thấy câu hỏi với id " + faqId));
        if (faq.getGroup().isMember(userDetails.getId())) {
            throw new ForbiddenException("Chỉ có thành viên của nhóm mới được vote");
        }
        var user = userRepository.findById(userDetails.getId()).orElseThrow(() -> new DomainException("Không tìm thấy người dùng với id " + userDetails.getId()));
        var voters = faq.getVoters();
        voters.add(user);
        faq.setVoters(voters);

        faqRepository.save(faq);
    }

    @Override
    public boolean downVote(CustomerUserDetails userDetails, String faqId) {
        var faq = faqRepository.findById(faqId).orElseThrow(() -> new DomainException("Không tìm thấy câu hỏi với id " + faqId));
        if (faq.getGroup().isMember(userDetails.getId())) {
            throw new ForbiddenException("Chỉ có thành viên của nhóm mới bỏ vote");
        }
        var user = userRepository.findById(userDetails.getId()).orElseThrow(() -> new DomainException("Không tìm thấy người dùng với id " + userDetails.getId()));

        var voters = faq.getVoters();
        voters.remove(user);
        faq.setVoters(voters);
        faqRepository.save(faq);

        return true;
    }
}