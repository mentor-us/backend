package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.NotificationSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationSubscriberRepository extends JpaRepository<NotificationSubscriber, String> {

    Optional<NotificationSubscriber> findByUserId(String userId);

    List<NotificationSubscriber> findByUserIdOrToken(String userId, String token);

    List<NotificationSubscriber> findAllByUserIdIn(List<String> userIds);

    void deleteByUserId(String userId);
}