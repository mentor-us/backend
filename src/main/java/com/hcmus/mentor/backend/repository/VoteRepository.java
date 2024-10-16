package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoteRepository extends JpaRepository<Vote, String> {

    List<Vote> findByGroupIdOrderByCreatedDateDesc(String groupId);
}
