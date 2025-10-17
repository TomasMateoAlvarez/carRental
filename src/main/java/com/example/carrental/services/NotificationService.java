package com.example.carrental.services;

import com.example.carrental.model.Notification;
import com.example.carrental.model.User;
import com.example.carrental.model.VehicleModel;
import com.example.carrental.repository.NotificationRepository;
import com.example.carrental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public Notification createNotification(Long userId, String type, String title, String message,
                                         String priority, String relatedEntityType, Long relatedEntityId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Notification notification = Notification.builder()
            .user(user)
            .type(type)
            .title(title)
            .message(message)
            .priority(priority != null ? priority : "MEDIUM")
            .relatedEntityType(relatedEntityType)
            .relatedEntityId(relatedEntityId)
            .isRead(false)
            .build();

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notification created for user {}: {}", user.getUsername(), title);

        return savedNotification;
    }

    @Async
    @Transactional
    public void createMaintenanceAlert(VehicleModel vehicle, String reason, int currentMileage) {
        // Find all admin and employee users to notify
        List<User> adminUsers = userRepository.findByRoleName("ADMIN");
        List<User> employeeUsers = userRepository.findByRoleName("EMPLOYEE");

        String title = "🔧 Alerta de Mantenimiento - " + vehicle.getBrand() + " " + vehicle.getModel();
        String message = String.format(
            "El vehículo %s (%s) requiere mantenimiento.\n" +
            "Kilometraje actual: %,d km\n" +
            "Razón: %s\n" +
            "Por favor, programa el mantenimiento lo antes posible.",
            vehicle.getLicensePlate(),
            vehicle.getBrand() + " " + vehicle.getModel(),
            currentMileage,
            reason
        );

        // Notify admins
        for (User admin : adminUsers) {
            createNotification(admin.getId(), "MAINTENANCE_DUE", title, message,
                             "HIGH", "VEHICLE", vehicle.getId());
        }

        // Notify employees
        for (User employee : employeeUsers) {
            createNotification(employee.getId(), "MAINTENANCE_DUE", title, message,
                             "HIGH", "VEHICLE", vehicle.getId());
        }

        log.info("Maintenance alerts sent for vehicle: {} to {} users",
                vehicle.getLicensePlate(), adminUsers.size() + employeeUsers.size());
    }

    @Async
    @Transactional
    public void createMaintenanceCompletedNotification(VehicleModel vehicle, String serviceProvider, String description) {
        // Find all admin users to notify
        List<User> adminUsers = userRepository.findByRoleName("ADMIN");

        String title = "✅ Mantenimiento Completado - " + vehicle.getBrand() + " " + vehicle.getModel();
        String message = String.format(
            "El mantenimiento del vehículo %s (%s) ha sido completado.\n" +
            "Proveedor de servicio: %s\n" +
            "Descripción: %s\n" +
            "El vehículo está listo para uso.",
            vehicle.getLicensePlate(),
            vehicle.getBrand() + " " + vehicle.getModel(),
            serviceProvider,
            description
        );

        for (User admin : adminUsers) {
            createNotification(admin.getId(), "VEHICLE_STATUS", title, message,
                             "MEDIUM", "VEHICLE", vehicle.getId());
        }

        log.info("Maintenance completion notifications sent for vehicle: {}", vehicle.getLicensePlate());
    }

    @Async
    @Transactional
    public void createVehicleStatusChangeNotification(VehicleModel vehicle, String oldStatus, String newStatus, Long changedByUserId) {
        // Find all admin users to notify
        List<User> adminUsers = userRepository.findByRoleName("ADMIN");

        String title = "🚗 Estado de Vehículo Cambiado - " + vehicle.getBrand() + " " + vehicle.getModel();
        String message = String.format(
            "El estado del vehículo %s (%s) ha cambiado.\n" +
            "Estado anterior: %s\n" +
            "Estado nuevo: %s\n" +
            "Cambiado por: Usuario ID %d",
            vehicle.getLicensePlate(),
            vehicle.getBrand() + " " + vehicle.getModel(),
            oldStatus,
            newStatus,
            changedByUserId
        );

        for (User admin : adminUsers) {
            createNotification(admin.getId(), "VEHICLE_STATUS", title, message,
                             "LOW", "VEHICLE", vehicle.getId());
        }
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);
    }

    public long getUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.countUnreadNotificationsByUser(user);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.markAsRead();
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        notificationRepository.markAllAsReadForUser(user, LocalDateTime.now());
        log.info("All notifications marked as read for user: {}", user.getUsername());
    }

    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    @Transactional
    public void cleanupExpiredNotifications() {
        int deletedCount = notificationRepository.deleteExpiredNotifications(LocalDateTime.now());
        if (deletedCount > 0) {
            log.info("Cleaned up {} expired notifications", deletedCount);
        }
    }

    public List<Notification> getNotificationsByType(String type) {
        return notificationRepository.findByTypeOrderByCreatedAtDesc(type);
    }

    public List<Notification> getHighPriorityNotifications() {
        return notificationRepository.findByPriorityOrderByCreatedAtDesc("HIGH");
    }
}