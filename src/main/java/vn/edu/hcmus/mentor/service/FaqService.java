package vn.edu.hcmus.mentor.service;

import vn.edu.hcmus.mentor.controller.payload.request.faqs.CreateFaqRequest;
import vn.edu.hcmus.mentor.controller.payload.request.faqs.ImportFAQsRequest;
import vn.edu.hcmus.mentor.controller.payload.request.faqs.UpdateFaqRequest;
import vn.edu.hcmus.mentor.controller.payload.response.FAQDetail;
import vn.edu.hcmus.mentor.domain.Faq;
import vn.edu.hcmus.mentor.security.principal.userdetails.CustomerUserDetails;

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
