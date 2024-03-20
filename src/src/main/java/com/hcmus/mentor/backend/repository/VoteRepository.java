package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoteRepository extends JpaRepository<Vote, String> {

    List<Vote> findByGroupIdOrderByCreatedDateDesc(String groupId);
}
