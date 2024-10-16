package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    @Query("SELECT n " +
            "FROM Notification n " +
            "JOIN n.receivers nu " +
            "WHERE nu.user.id IN ?1 " +
            "ORDER BY n.createdDate DESC")
    Page<Notification> findOwnNotifications(List<String> receiverIds, Pageable pageable);
}