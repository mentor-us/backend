package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, String> {
}