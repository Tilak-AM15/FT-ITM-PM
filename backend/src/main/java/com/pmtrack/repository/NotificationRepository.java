package com.pmtrack.repository;

import com.pmtrack.model.Notification;
import com.pmtrack.model.NotificationType;
import com.pmtrack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientOrderByCreatedAtDesc(
            User recipient
    );

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(
            Long recipientId
    );

    List<Notification> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(
            Long recipientId
    );

    long countByRecipientIdAndIsReadFalse(
            Long recipientId
    );

    boolean existsByRecipientIdAndTypeAndCreatedAtBetween(
            Long recipientId,
            NotificationType type,
            LocalDateTime start,
            LocalDateTime end
    );

    Optional<Notification> findByIdAndRecipientId(
            Long notificationId,
            Long recipientId
    );
}
