package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Faq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FaqRepository extends JpaRepository<Faq, String> {

    List<Faq> findByIdIn(List<String> faqIds);

    List<Faq> findByGroupId(String groupId);
}
