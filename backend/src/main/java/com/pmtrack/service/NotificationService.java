package com.pmtrack.service;

import com.pmtrack.model.Notification;
import com.pmtrack.model.NotificationType;
import com.pmtrack.model.User;
import com.pmtrack.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Notification createNotification(
            User recipient,
            String title,
            String message,
            NotificationType type,
            String linkUrl) {

        Notification notification = new Notification(
                recipient,
                title,
                message,
                type,
                linkUrl
        );

        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(Long userId) {

        return notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {

        return notificationRepository
                .countByRecipientIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {

        notificationRepository
                .findByIdAndRecipientId(notificationId, userId)
                .ifPresent(notification -> {
                    notification.setRead(true);
                    notificationRepository.save(notification);
                });
    }

    @Transactional
    public void markAllAsRead(Long userId) {

        List<Notification> unread =
                notificationRepository
                        .findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId);

        for (Notification notification : unread) {
            notification.setRead(true);
        }

        if (!unread.isEmpty()) {
            notificationRepository.saveAll(unread);
        }
    }
}
