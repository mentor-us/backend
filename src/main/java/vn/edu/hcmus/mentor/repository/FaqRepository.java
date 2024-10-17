package vn.edu.hcmus.mentor.repository;

import vn.edu.hcmus.mentor.domain.Faq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaqRepository extends JpaRepository<Faq, String> {

    List<Faq> findByIdIn(List<String> faqIds);

    List<Faq> findByGroupId(String groupId);
}
