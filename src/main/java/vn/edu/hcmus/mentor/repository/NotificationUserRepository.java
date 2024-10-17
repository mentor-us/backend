package vn.edu.hcmus.mentor.repository;

import vn.edu.hcmus.mentor.domain.NotificationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationUserRepository extends JpaRepository<NotificationUser, String> {

    long countByUserIdAndIsDeletedIsFalse(String userId);
}
