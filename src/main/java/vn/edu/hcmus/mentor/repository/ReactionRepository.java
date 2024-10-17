package vn.edu.hcmus.mentor.repository;

import vn.edu.hcmus.mentor.domain.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, String> {
}