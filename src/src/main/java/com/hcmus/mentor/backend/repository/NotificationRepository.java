package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Notification;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, String> {

    Slice<Notification> findByReceiverIdsIn(List<String> receiverId, PageRequest paging);

    long countDistinctByReceiverIdsIn(List<String> userIds);
}
