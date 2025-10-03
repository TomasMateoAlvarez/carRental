package com.example.carrental.services;

import com.example.carrental.model.Reservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SMSService {

    @Value("${twilio.account.sid:}")
    private String twilioAccountSid;

    @Value("${twilio.auth.token:}")
    private String twilioAuthToken;

    @Value("${twilio.phone.number:+1234567890}")
    private String twilioPhoneNumber;

    @Value("${app.name:CarRental SaaS}")
    private String appName;

    public void sendReservationConfirmation(Reservation reservation) {
        try {
            String message = String.format(
                "✅ %s - Tu reserva %s ha sido confirmada. Vehículo: %s %s. Fecha: %s al %s. ¡Gracias!",
                appName,
                reservation.getReservationCode(),
                reservation.getVehicle().getBrand(),
                reservation.getVehicle().getModel(),
                reservation.getStartDate(),
                reservation.getEndDate()
            );

            sendSMS(reservation.getUser().getPhoneNumber(), message);
            log.info("SMS reservation confirmation sent to {}", reservation.getUser().getPhoneNumber());
        } catch (Exception e) {
            log.error("Failed to send SMS reservation confirmation", e);
        }
    }

    public void sendPickupReminder(Reservation reservation, int hoursBeforePickup) {
        try {
            String message = String.format(
                "🚗 %s - Recordatorio: Tu reserva %s es en %d horas. Vehículo: %s %s. ¡No olvides recogerlo!",
                appName,
                reservation.getReservationCode(),
                hoursBeforePickup,
                reservation.getVehicle().getBrand(),
                reservation.getVehicle().getModel()
            );

            sendSMS(reservation.getUser().getPhoneNumber(), message);
            log.info("SMS pickup reminder sent to {}", reservation.getUser().getPhoneNumber());
        } catch (Exception e) {
            log.error("Failed to send SMS pickup reminder", e);
        }
    }

    public void sendReturnReminder(Reservation reservation, int hoursBeforeReturn) {
        try {
            String message = String.format(
                "⏰ %s - Recordatorio: Debes devolver tu vehículo %s %s en %d horas. Reserva: %s",
                appName,
                reservation.getVehicle().getBrand(),
                reservation.getVehicle().getModel(),
                hoursBeforeReturn,
                reservation.getReservationCode()
            );

            sendSMS(reservation.getUser().getPhoneNumber(), message);
            log.info("SMS return reminder sent to {}", reservation.getUser().getPhoneNumber());
        } catch (Exception e) {
            log.error("Failed to send SMS return reminder", e);
        }
    }

    public void sendPaymentConfirmation(String phoneNumber, String paymentCode, String amount) {
        try {
            String message = String.format(
                "💳 %s - Pago confirmado. Código: %s. Monto: %s. ¡Gracias por tu confianza!",
                appName,
                paymentCode,
                amount
            );

            sendSMS(phoneNumber, message);
            log.info("SMS payment confirmation sent to {}", phoneNumber);
        } catch (Exception e) {
            log.error("Failed to send SMS payment confirmation", e);
        }
    }

    public void sendSMS(String phoneNumber, String message) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            log.warn("Cannot send SMS: phone number is empty");
            return;
        }

        if (twilioAccountSid.isEmpty() || twilioAuthToken.isEmpty()) {
            log.info("SMS Service not configured (Twilio credentials missing). Would send SMS to {}: {}",
                    phoneNumber, message);
            return;
        }

        try {
            // In production, use Twilio SDK:
            // Twilio.init(twilioAccountSid, twilioAuthToken);
            // Message twilioMessage = Message.creator(
            //         new PhoneNumber(phoneNumber),
            //         new PhoneNumber(twilioPhoneNumber),
            //         message
            // ).create();

            // For now, just log the SMS
            log.info("SMS sent to {}: {}", phoneNumber, message);

        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage());
            throw new RuntimeException("SMS sending failed", e);
        }
    }

    public void sendBulkSMS(java.util.List<String> phoneNumbers, String message) {
        phoneNumbers.forEach(phoneNumber -> {
            try {
                sendSMS(phoneNumber, message);
                // Add delay to avoid rate limiting
                Thread.sleep(100);
            } catch (Exception e) {
                log.error("Failed to send bulk SMS to {}", phoneNumber, e);
            }
        });
    }

    public void sendPromotionalSMS(String phoneNumber, String promoCode, String discount) {
        try {
            String message = String.format(
                "🎉 %s - ¡Oferta especial! Usa el código %s y obtén %s de descuento. ¡No te lo pierdas!",
                appName,
                promoCode,
                discount
            );

            sendSMS(phoneNumber, message);
            log.info("Promotional SMS sent to {}", phoneNumber);
        } catch (Exception e) {
            log.error("Failed to send promotional SMS", e);
        }
    }
}