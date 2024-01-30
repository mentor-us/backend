package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Notif;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationRepository extends MongoRepository<Notif, String> {

    Slice<Notif> findByReceiverIdsIn(List<String> receiverId, PageRequest paging);

    long countDistinctByReceiverIdsIn(List<String> userIds);
}
