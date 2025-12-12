package org.example.notification.repository;

import org.example.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataNotificationRepository extends JpaRepository<Notification, String> {

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.deleted = false ORDER BY n.createdAt DESC")
    Page<Notification> findByUserId(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isRead = :isRead AND n.deleted = false ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdAndIsRead(@Param("userId") String userId, @Param("isRead") boolean isRead, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false AND n.deleted = false")
    long countUnreadByUserId(@Param("userId") String userId);
}