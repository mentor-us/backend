package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.entity.NotificationSubscriber;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationSubscriberRepository extends MongoRepository<NotificationSubscriber, String> {

    Optional<NotificationSubscriber> findByUserId(String userId);

    List<NotificationSubscriber> findByUserIdOrToken(String userId, String token);

    void deleteByUserId(String userId);

    List<NotificationSubscriber> findAllByUserIdIn(List<String> userIds);
}
