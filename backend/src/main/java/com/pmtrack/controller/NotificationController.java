package com.pmtrack.controller;

import com.pmtrack.model.Notification;
import com.pmtrack.security.UserPrincipal;
import com.pmtrack.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "In-app notifications and alerts")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "Get notifications for current user")
    public ResponseEntity<List<Notification>> getMyNotifications(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        return ResponseEntity.ok(
                notificationService.getUserNotifications(userPrincipal.getId())
        );
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count for badge")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        return ResponseEntity.ok(
                Map.of(
                        "unreadCount",
                        notificationService.getUnreadCount(userPrincipal.getId())
                )
        );
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark single notification as read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        notificationService.markAsRead(
                id,
                userPrincipal.getId()
        );

        return ResponseEntity.ok(
                Map.of("message", "Marked as read")
        );
    }

    @PostMapping("/mark-all-read")
    @Operation(summary = "Mark all notifications as read for current user")
    public ResponseEntity<?> markAllAsRead(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        notificationService.markAllAsRead(
                userPrincipal.getId()
        );

        return ResponseEntity.ok(
                Map.of("message", "All marked as read")
        );
    }
}
