package com.hcmus.mentor.backend.usercase.common.repository;

import com.hcmus.mentor.backend.entity.FAQ;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FaqRepository extends MongoRepository<FAQ, String> {

  List<FAQ> findByIdIn(List<String> faqIds);

  List<FAQ> findByGroupId(String groupId);
}
