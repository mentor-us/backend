package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.domain.Faq;
import com.hcmus.mentor.backend.controller.payload.request.faqs.CreateFaqRequest;
import com.hcmus.mentor.backend.controller.payload.request.faqs.ImportFAQsRequest;
import com.hcmus.mentor.backend.controller.payload.request.faqs.UpdateFaqRequest;
import com.hcmus.mentor.backend.controller.payload.response.FAQDetail;
import com.hcmus.mentor.backend.security.UserPrincipal;

import java.util.List;

public interface FaqService {
    List<Faq> getByGroupId(String userId, String groupId);

    FAQDetail getById(String userId, String faqId);

    Faq addNewFaq(String userId, CreateFaqRequest request);

    Faq updateFAQ(String userId, String faqId, UpdateFaqRequest request);

    boolean deleteFaq(String userId, String faqId);

    void importFAQs(UserPrincipal user, String toGroupId, ImportFAQsRequest request);

    boolean upvote(UserPrincipal user, String faqId);

    boolean downVote(UserPrincipal user, String faqId);
}
