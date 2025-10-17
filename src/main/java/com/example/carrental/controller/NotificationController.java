package com.example.carrental.controller;

import com.example.carrental.model.Notification;
import com.example.carrental.model.User;
import com.example.carrental.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/create")
    @PreAuthorize("hasPermission('NOTIFICATION_MANAGE', 'CREATE')")
    public ResponseEntity<Notification> createNotification(
            @RequestParam("userId") Long userId,
            @RequestParam("type") String type,
            @RequestParam("title") String title,
            @RequestParam("message") String message,
            @RequestParam(value = "priority", required = false) String priority,
            @RequestParam(value = "relatedEntityType", required = false) String relatedEntityType,
            @RequestParam(value = "relatedEntityId", required = false) Long relatedEntityId) {

        try {
            Notification notification = notificationService.createNotification(
                userId, type, title, message, priority, relatedEntityType, relatedEntityId
            );
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            log.error("Error creating notification: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user")
    public ResponseEntity<List<Notification>> getUserNotifications(@AuthenticationPrincipal User currentUser) {
        try {
            List<Notification> notifications = notificationService.getUserNotifications(currentUser.getId());
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("Error getting notifications for user {}: {}", currentUser.getId(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasPermission('NOTIFICATION_MANAGE', 'READ')")
    public ResponseEntity<List<Notification>> getUserNotificationsById(@PathVariable Long userId) {
        try {
            List<Notification> notifications = notificationService.getUserNotifications(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("Error getting notifications for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@AuthenticationPrincipal User currentUser) {
        try {
            List<Notification> notifications = notificationService.getUnreadNotifications(currentUser.getId());
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("Error getting unread notifications for user {}: {}", currentUser.getId(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/unread/count")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal User currentUser) {
        try {
            long count = notificationService.getUnreadCount(currentUser.getId());
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Error getting unread count for user {}: {}", currentUser.getId(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasPermission('NOTIFICATION_MANAGE', 'READ')")
    public ResponseEntity<List<Notification>> getNotificationsByType(@PathVariable String type) {
        try {
            List<Notification> notifications = notificationService.getNotificationsByType(type);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("Error getting notifications by type {}: {}", type, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/priority/high")
    @PreAuthorize("hasPermission('NOTIFICATION_MANAGE', 'READ')")
    public ResponseEntity<List<Notification>> getHighPriorityNotifications() {
        try {
            List<Notification> notifications = notificationService.getHighPriorityNotifications();
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("Error getting high priority notifications: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{notificationId}/mark-read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal User currentUser) {
        try {
            // TODO: Add security check to ensure user owns the notification
            notificationService.markAsRead(notificationId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error marking notification {} as read: {}", notificationId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/user/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal User currentUser) {
        try {
            notificationService.markAllAsRead(currentUser.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error marking all notifications as read for user {}: {}", currentUser.getId(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal User currentUser) {
        try {
            // TODO: Add security check to ensure user owns the notification
            notificationService.deleteNotification(notificationId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting notification {}: {}", notificationId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/cleanup-expired")
    @PreAuthorize("hasPermission('NOTIFICATION_MANAGE', 'DELETE')")
    public ResponseEntity<Void> cleanupExpiredNotifications() {
        try {
            notificationService.cleanupExpiredNotifications();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error cleaning up expired notifications: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}