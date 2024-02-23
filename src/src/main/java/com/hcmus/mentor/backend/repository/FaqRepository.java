package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Faq;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface FaqRepository extends MongoRepository<Faq, String> {

    List<Faq> findByIdIn(List<String> faqIds);

    List<Faq> findByGroupId(String groupId);
}
