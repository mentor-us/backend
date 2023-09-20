package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.entity.Vote;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface VoteRepository extends MongoRepository<Vote, String> {

    List<Vote> findByGroupIdOrderByCreatedDateDesc(String groupId);
}
