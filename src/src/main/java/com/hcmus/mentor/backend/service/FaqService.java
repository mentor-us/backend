package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.controller.payload.request.faqs.CreateFaqRequest;
import com.hcmus.mentor.backend.controller.payload.request.faqs.ImportFAQsRequest;
import com.hcmus.mentor.backend.controller.payload.request.faqs.UpdateFaqRequest;
import com.hcmus.mentor.backend.controller.payload.response.FAQDetail;
import com.hcmus.mentor.backend.domain.Faq;
import com.hcmus.mentor.backend.security.principal.userdetails.CustomerUserDetails;

import java.util.List;

public interface FaqService {
    List<Faq> getByGroupId(String userId, String groupId);

    FAQDetail getById(String userId, String faqId);

    Faq createFaq(String userId, CreateFaqRequest request);

    Faq updateFAQ(String userId, String faqId, UpdateFaqRequest request);

    void deleteFaq(String userId, String faqId);

    void importFaqs(String creatorId, String destGroupId, ImportFAQsRequest request);

    void upvote(CustomerUserDetails user, String faqId);

    boolean downVote(CustomerUserDetails user, String faqId);
}
