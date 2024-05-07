package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.NotificationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationUserRepository extends JpaRepository<NotificationUser, String> {
}
