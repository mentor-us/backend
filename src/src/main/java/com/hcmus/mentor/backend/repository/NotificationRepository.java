package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.entity.Notif;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notif, String> {

    Slice<Notif> findByReceiverIdsIn(List<String> receiverId, PageRequest paging);

    long countDistinctByReceiverIdsIn(List<String> userIds);
}
