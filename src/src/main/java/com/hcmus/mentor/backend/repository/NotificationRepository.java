package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    Slice<Notification> findByReceiverIdsIn(List<String> receiverId, PageRequest paging);

    long countDistinctByReceiverIdsIn(List<String> userIds);

    @Query("SELECT n FROM Notification n JOIN n.receivers receivers WHERE receivers.id IN :receiverId ORDER BY n.createdDate DESC")
    Page<Notification> findOwnNotifications(List<String> receiverId, Pageable pageable);
}
