package vn.edu.hcmus.mentor.repository;

import vn.edu.hcmus.mentor.domain.Choice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChoiceRepository extends JpaRepository<Choice, String> {
}
