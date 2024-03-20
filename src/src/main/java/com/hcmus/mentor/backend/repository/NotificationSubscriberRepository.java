package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.NotificationSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationSubscriberRepository
        extends JpaRepository<NotificationSubscriber, String> {

    Optional<NotificationSubscriber> findByUserId(String userId);

    List<NotificationSubscriber> findByUserIdOrToken(String userId, String token);

    void deleteByUserId(String userId);

    List<NotificationSubscriber> findAllByUserIdIn(List<String> userIds);
}
