package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Choice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChoiceRepository extends JpaRepository<Choice, String> {
}
