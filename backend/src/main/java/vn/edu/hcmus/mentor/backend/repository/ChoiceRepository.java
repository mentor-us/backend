package vn.edu.hcmus.mentor.backend.repository;

import vn.edu.hcmus.mentor.backend.domain.Choice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChoiceRepository extends JpaRepository<Choice, String> {
}
