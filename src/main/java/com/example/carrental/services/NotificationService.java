package com.example.carrental.services;

import com.example.carrental.model.Payment;
import com.example.carrental.model.Reservation;
import com.example.carrental.model.User;
import com.example.carrental.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final SMSService smsService;

    @Value("${app.mail.from:noreply@carrental.com}")
    private String fromEmail;

    @Value("${app.name:CarRental SaaS}")
    private String appName;

    @Value("${app.url:https://carrental.com}")
    private String appUrl;

    // Email Notifications
    @Async
    public CompletableFuture<Void> sendReservationConfirmation(Reservation reservation) {
        try {
            Context context = new Context();
            context.setVariable("reservation", reservation);
            context.setVariable("user", reservation.getUser());
            context.setVariable("vehicle", reservation.getVehicle());
            context.setVariable("appName", appName);
            context.setVariable("appUrl", appUrl);

            String htmlContent = templateEngine.process("email/reservation-confirmation", context);

            sendEmail(
                reservation.getUser().getEmail(),
                "Confirmación de Reserva - " + reservation.getReservationCode(),
                htmlContent
            );

            // Send SMS if user has phone
            if (reservation.getUser().getPhoneNumber() != null) {
                smsService.sendReservationConfirmation(reservation);
            }

            log.info("Sent reservation confirmation for {}", reservation.getReservationCode());
        } catch (Exception e) {
            log.error("Failed to send reservation confirmation", e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> sendPaymentConfirmation(Payment payment) {
        try {
            Context context = new Context();
            context.setVariable("payment", payment);
            context.setVariable("reservation", payment.getReservation());
            context.setVariable("user", payment.getUser());
            context.setVariable("appName", appName);
            context.setVariable("appUrl", appUrl);

            String htmlContent = templateEngine.process("email/payment-confirmation", context);

            sendEmail(
                payment.getUser().getEmail(),
                "Confirmación de Pago - " + payment.getPaymentCode(),
                htmlContent
            );

            log.info("Sent payment confirmation for {}", payment.getPaymentCode());
        } catch (Exception e) {
            log.error("Failed to send payment confirmation", e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> sendRefundConfirmation(Payment payment, BigDecimal refundAmount) {
        try {
            Context context = new Context();
            context.setVariable("payment", payment);
            context.setVariable("refundAmount", refundAmount);
            context.setVariable("user", payment.getUser());
            context.setVariable("appName", appName);

            String htmlContent = templateEngine.process("email/refund-confirmation", context);

            sendEmail(
                payment.getUser().getEmail(),
                "Confirmación de Reembolso - " + payment.getPaymentCode(),
                htmlContent
            );

            log.info("Sent refund confirmation for {} - Amount: {}", payment.getPaymentCode(), refundAmount);
        } catch (Exception e) {
            log.error("Failed to send refund confirmation", e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> sendReminderNotification(Reservation reservation, int hoursBeforePickup) {
        try {
            Context context = new Context();
            context.setVariable("reservation", reservation);
            context.setVariable("user", reservation.getUser());
            context.setVariable("vehicle", reservation.getVehicle());
            context.setVariable("hoursBeforePickup", hoursBeforePickup);
            context.setVariable("appName", appName);

            String htmlContent = templateEngine.process("email/pickup-reminder", context);

            sendEmail(
                reservation.getUser().getEmail(),
                "Recordatorio de Recogida - " + reservation.getReservationCode(),
                htmlContent
            );

            // Send SMS reminder
            if (reservation.getUser().getPhoneNumber() != null) {
                smsService.sendPickupReminder(reservation, hoursBeforePickup);
            }

            log.info("Sent pickup reminder for {} - {} hours before",
                    reservation.getReservationCode(), hoursBeforePickup);
        } catch (Exception e) {
            log.error("Failed to send pickup reminder", e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> sendWelcomeEmail(User user) {
        try {
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("appName", appName);
            context.setVariable("appUrl", appUrl);

            String htmlContent = templateEngine.process("email/welcome", context);

            sendEmail(
                user.getEmail(),
                "¡Bienvenido a " + appName + "!",
                htmlContent
            );

            log.info("Sent welcome email to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send welcome email", e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> sendMaintenanceNotification(String vehicleLicensePlate, LocalDateTime maintenanceDate) {
        try {
            Context context = new Context();
            context.setVariable("vehicleLicensePlate", vehicleLicensePlate);
            context.setVariable("maintenanceDate", maintenanceDate);
            context.setVariable("appName", appName);

            String htmlContent = templateEngine.process("email/maintenance-notification", context);

            // Send to admin emails (could be configured)
            sendEmail(
                "admin@carrental.com",
                "Notificación de Mantenimiento - " + vehicleLicensePlate,
                htmlContent
            );

            log.info("Sent maintenance notification for vehicle {}", vehicleLicensePlate);
        } catch (Exception e) {
            log.error("Failed to send maintenance notification", e);
        }
        return CompletableFuture.completedFuture(null);
    }

    // Marketing & Business Notifications
    @Async
    public CompletableFuture<Void> sendPromotionalEmail(User user, String promoCode, String discountDescription) {
        try {
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("promoCode", promoCode);
            context.setVariable("discountDescription", discountDescription);
            context.setVariable("appName", appName);
            context.setVariable("appUrl", appUrl);

            String htmlContent = templateEngine.process("email/promotional", context);

            sendEmail(
                user.getEmail(),
                "¡Oferta Especial en " + appName + "!",
                htmlContent
            );

            log.info("Sent promotional email to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send promotional email", e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> sendLoyaltyRewardEmail(User user, int points, String rewardDescription) {
        try {
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("points", points);
            context.setVariable("rewardDescription", rewardDescription);
            context.setVariable("appName", appName);

            String htmlContent = templateEngine.process("email/loyalty-reward", context);

            sendEmail(
                user.getEmail(),
                "¡Has ganado puntos de lealtad!",
                htmlContent
            );

            log.info("Sent loyalty reward email to {} - {} points", user.getEmail(), points);
        } catch (Exception e) {
            log.error("Failed to send loyalty reward email", e);
        }
        return CompletableFuture.completedFuture(null);
    }

    // Business Intelligence Notifications
    @Async
    public CompletableFuture<Void> sendDailyReportEmail(Map<String, Object> reportData) {
        try {
            Context context = new Context();
            context.setVariable("reportData", reportData);
            context.setVariable("appName", appName);
            context.setVariable("reportDate", LocalDateTime.now());

            String htmlContent = templateEngine.process("email/daily-report", context);

            sendEmail(
                "admin@carrental.com",
                "Reporte Diario - " + appName,
                htmlContent
            );

            log.info("Sent daily report email");
        } catch (Exception e) {
            log.error("Failed to send daily report email", e);
        }
        return CompletableFuture.completedFuture(null);
    }

    // Utility method for sending emails
    private void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    // Multi-channel notification method
    @Async
    public CompletableFuture<Void> sendMultiChannelNotification(
            User user,
            String subject,
            String message,
            NotificationType type,
            Map<String, Object> templateData
    ) {
        try {
            // Email
            if (user.isEmailNotificationsEnabled()) {
                Context context = new Context();
                templateData.forEach(context::setVariable);
                context.setVariable("user", user);
                context.setVariable("appName", appName);

                String template = getTemplateForNotificationType(type);
                String htmlContent = templateEngine.process(template, context);

                sendEmail(user.getEmail(), subject, htmlContent);
            }

            // SMS
            if (user.isSmsNotificationsEnabled() && user.getPhoneNumber() != null) {
                smsService.sendSMS(user.getPhoneNumber(), subject + ": " + message);
            }

            // Push notification (if mobile app is installed)
            if (user.isPushNotificationsEnabled() && user.getDeviceToken() != null) {
                // pushNotificationService.sendPushNotification(user.getDeviceToken(), subject, message);
            }

            log.info("Sent multi-channel notification to user {}: {}", user.getId(), subject);
        } catch (Exception e) {
            log.error("Failed to send multi-channel notification", e);
        }
        return CompletableFuture.completedFuture(null);
    }

    private String getTemplateForNotificationType(NotificationType type) {
        return switch (type) {
            case RESERVATION_CONFIRMATION -> "email/reservation-confirmation";
            case PAYMENT_CONFIRMATION -> "email/payment-confirmation";
            case PICKUP_REMINDER -> "email/pickup-reminder";
            case RETURN_REMINDER -> "email/return-reminder";
            case PROMOTIONAL -> "email/promotional";
            case MAINTENANCE_ALERT -> "email/maintenance-notification";
            default -> "email/generic-notification";
        };
    }
}