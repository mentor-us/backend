package com.hcmus.mentor.backend.usercase.common.service;

import com.hcmus.mentor.backend.entity.FAQ;
import com.hcmus.mentor.backend.payload.request.faqs.CreateFaqRequest;
import com.hcmus.mentor.backend.payload.request.faqs.ImportFAQsRequest;
import com.hcmus.mentor.backend.payload.request.faqs.UpdateFaqRequest;
import com.hcmus.mentor.backend.payload.response.FAQDetail;
import com.hcmus.mentor.backend.web.infrastructure.security.UserPrincipal;
import java.util.List;

public interface FaqService {
  List<FAQ> getByGroupId(String userId, String groupId);

  FAQDetail getById(String userId, String faqId);

  FAQ addNewFaq(String userId, CreateFaqRequest request);

  FAQ updateFAQ(String userId, String faqId, UpdateFaqRequest request);

  boolean deleteFaq(String userId, String faqId);

  void importFAQs(UserPrincipal user, String toGroupId, ImportFAQsRequest request);

  boolean upvote(UserPrincipal user, String faqId);

  boolean downVote(UserPrincipal user, String faqId);
}
