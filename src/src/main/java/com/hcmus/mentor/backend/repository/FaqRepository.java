package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.entity.FAQ;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FaqRepository extends MongoRepository<FAQ, String> {

    List<FAQ> findByIdIn(List<String> faqIds);

    List<FAQ> findByGroupId(String groupId);
}
