package vn.edu.hcmus.mentor.repository;

import vn.edu.hcmus.mentor.domain.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoteRepository extends JpaRepository<Vote, String> {

    List<Vote> findByGroupIdOrderByCreatedDateDesc(String groupId);
}
