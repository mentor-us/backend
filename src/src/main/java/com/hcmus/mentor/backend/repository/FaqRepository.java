package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Faq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaqRepository extends JpaRepository<Faq, String> {

    List<Faq> findByIdIn(List<String> faqIds);

    List<Faq> findByGroupId(String groupId);
}
