package com.hcmus.mentor.backend.usercase.common.repository;

import com.hcmus.mentor.backend.entity.Vote;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface VoteRepository extends MongoRepository<Vote, String> {

  List<Vote> findByGroupIdOrderByCreatedDateDesc(String groupId);
}
