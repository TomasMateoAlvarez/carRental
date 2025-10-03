package com.example.carrental.services;

import com.example.carrental.dto.PaymentRequestDTO;
import com.example.carrental.dto.PaymentResponseDTO;
import com.example.carrental.dto.RefundRequestDTO;
import com.example.carrental.enums.PaymentMethod;
import com.example.carrental.enums.PaymentStatus;
import com.example.carrental.model.Payment;
import com.example.carrental.model.Reservation;
import com.example.carrental.model.User;
import com.example.carrental.repository.PaymentRepository;
import com.example.carrental.repository.ReservationRepository;
import com.example.carrental.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.publishable-key}")
    private String stripePublishableKey;

    @Value("${business.pricing.tax-rate:0.08}")
    private Double taxRate;

    @Value("${business.pricing.processing-fee-rate:0.029}")
    private Double processingFeeRate;

    private void initializeStripe() {
        Stripe.apiKey = stripeSecretKey;
    }

    public PaymentResponseDTO createPaymentIntent(PaymentRequestDTO request) {
        try {
            initializeStripe();

            // Validate reservation
            Reservation reservation = reservationRepository.findById(request.getReservationId())
                    .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Calculate payment breakdown
            PaymentBreakdown breakdown = calculatePaymentBreakdown(
                    reservation.getTotalAmount(), request.getDiscountAmount());

            // Create Stripe customer if doesn't exist
            Customer stripeCustomer = createOrUpdateStripeCustomer(user);

            // Create Payment Intent
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(breakdown.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue()) // Stripe uses cents
                    .setCurrency("usd")
                    .setCustomer(stripeCustomer.getId())
                    .setDescription("Car Rental Payment - Reservation " + reservation.getReservationCode())
                    .putMetadata("reservation_id", reservation.getId().toString())
                    .putMetadata("user_id", user.getId().toString())
                    .putMetadata("reservation_code", reservation.getReservationCode())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            // Create payment record
            Payment payment = Payment.builder()
                    .paymentCode(generatePaymentCode())
                    .reservation(reservation)
                    .user(user)
                    .amount(breakdown.getTotalAmount())
                    .currency("USD")
                    .status(PaymentStatus.PENDING)
                    .paymentMethod(PaymentMethod.CREDIT_CARD)
                    .stripePaymentIntentId(intent.getId())
                    .stripeCustomerId(stripeCustomer.getId())
                    .description("Payment for reservation " + reservation.getReservationCode())
                    .subtotal(breakdown.getSubtotal())
                    .taxAmount(breakdown.getTaxAmount())
                    .discountAmount(breakdown.getDiscountAmount())
                    .processingFee(breakdown.getProcessingFee())
                    .build();

            payment = paymentRepository.save(payment);

            log.info("Created payment intent {} for reservation {}", intent.getId(), reservation.getReservationCode());

            return PaymentResponseDTO.builder()
                    .paymentId(payment.getId())
                    .paymentCode(payment.getPaymentCode())
                    .clientSecret(intent.getClientSecret())
                    .amount(breakdown.getTotalAmount())
                    .subtotal(breakdown.getSubtotal())
                    .taxAmount(breakdown.getTaxAmount())
                    .discountAmount(breakdown.getDiscountAmount())
                    .processingFee(breakdown.getProcessingFee())
                    .status(payment.getStatus().name())
                    .stripePublishableKey(stripePublishableKey)
                    .build();

        } catch (StripeException e) {
            log.error("Stripe error creating payment intent: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating payment: " + e.getUserMessage());
        }
    }

    public PaymentResponseDTO confirmPayment(String paymentIntentId) {
        try {
            initializeStripe();

            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            if ("succeeded".equals(intent.getStatus())) {
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setPaidAt(LocalDateTime.now());
                payment.setStripeChargeId(intent.getLatestCharge());

                paymentRepository.save(payment);

                // Update reservation status if needed
                Reservation reservation = payment.getReservation();
                if (reservation.getStatus() == com.example.carrental.enums.ReservationStatus.PENDING) {
                    reservation.setStatus(com.example.carrental.enums.ReservationStatus.CONFIRMED);
                    reservation.setConfirmedAt(LocalDateTime.now());
                    reservationRepository.save(reservation);
                }

                // Send confirmation notification
                notificationService.sendPaymentConfirmation(payment);

                log.info("Payment {} confirmed successfully", payment.getPaymentCode());

                return convertToDTO(payment);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailedAt(LocalDateTime.now());
                payment.setFailureReason("Payment intent status: " + intent.getStatus());

                paymentRepository.save(payment);

                throw new RuntimeException("Payment failed with status: " + intent.getStatus());
            }

        } catch (StripeException e) {
            log.error("Stripe error confirming payment: {}", e.getMessage(), e);
            throw new RuntimeException("Error confirming payment: " + e.getUserMessage());
        }
    }

    public PaymentResponseDTO refundPayment(RefundRequestDTO request) {
        try {
            initializeStripe();

            Payment payment = paymentRepository.findById(request.getPaymentId())
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            if (!payment.canBeRefunded()) {
                throw new RuntimeException("Payment cannot be refunded");
            }

            // Calculate refund amount
            BigDecimal refundAmount = request.getAmount() != null
                    ? request.getAmount()
                    : payment.getRefundableAmount();

            // Create Stripe refund
            RefundCreateParams params = RefundCreateParams.builder()
                    .setCharge(payment.getStripeChargeId())
                    .setAmount(refundAmount.multiply(BigDecimal.valueOf(100)).longValue()) // Stripe uses cents
                    .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                    .putMetadata("payment_id", payment.getId().toString())
                    .putMetadata("reason", request.getReason())
                    .build();

            Refund refund = Refund.create(params);

            // Update payment
            BigDecimal currentRefundAmount = payment.getRefundAmount() != null
                    ? payment.getRefundAmount()
                    : BigDecimal.ZERO;

            BigDecimal newRefundAmount = currentRefundAmount.add(refundAmount);
            payment.setRefundAmount(newRefundAmount);
            payment.setRefundReason(request.getReason());
            payment.setStripeRefundId(refund.getId());
            payment.setRefundedAt(LocalDateTime.now());

            // Update status
            if (newRefundAmount.compareTo(payment.getAmount()) >= 0) {
                payment.setStatus(PaymentStatus.REFUNDED);
            } else {
                payment.setStatus(PaymentStatus.PARTIALLY_REFUNDED);
            }

            paymentRepository.save(payment);

            // Send refund notification
            notificationService.sendRefundConfirmation(payment, refundAmount);

            log.info("Refund processed for payment {} - Amount: {}",
                    payment.getPaymentCode(), refundAmount);

            return convertToDTO(payment);

        } catch (StripeException e) {
            log.error("Stripe error processing refund: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing refund: " + e.getUserMessage());
        }
    }

    private Customer createOrUpdateStripeCustomer(User user) throws StripeException {
        // Check if customer already exists
        if (user.getStripeCustomerId() != null) {
            try {
                return Customer.retrieve(user.getStripeCustomerId());
            } catch (StripeException e) {
                log.warn("Stripe customer {} not found, creating new one", user.getStripeCustomerId());
            }
        }

        // Create new customer
        CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(user.getEmail())
                .setName(user.getFirstName() + " " + user.getLastName())
                .putMetadata("user_id", user.getId().toString())
                .build();

        Customer customer = Customer.create(params);

        // Update user with Stripe customer ID
        user.setStripeCustomerId(customer.getId());
        userRepository.save(user);

        return customer;
    }

    private PaymentBreakdown calculatePaymentBreakdown(BigDecimal baseAmount, BigDecimal discountAmount) {
        BigDecimal subtotal = baseAmount;
        BigDecimal discount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        BigDecimal discountedAmount = subtotal.subtract(discount);

        BigDecimal taxAmount = discountedAmount.multiply(BigDecimal.valueOf(taxRate))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalBeforeFees = discountedAmount.add(taxAmount);

        BigDecimal processingFee = totalBeforeFees.multiply(BigDecimal.valueOf(processingFeeRate))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalAmount = totalBeforeFees.add(processingFee);

        return PaymentBreakdown.builder()
                .subtotal(subtotal)
                .discountAmount(discount)
                .taxAmount(taxAmount)
                .processingFee(processingFee)
                .totalAmount(totalAmount)
                .build();
    }

    private String generatePaymentCode() {
        return "PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private PaymentResponseDTO convertToDTO(Payment payment) {
        return PaymentResponseDTO.builder()
                .paymentId(payment.getId())
                .paymentCode(payment.getPaymentCode())
                .amount(payment.getAmount())
                .subtotal(payment.getSubtotal())
                .taxAmount(payment.getTaxAmount())
                .discountAmount(payment.getDiscountAmount())
                .processingFee(payment.getProcessingFee())
                .status(payment.getStatus().name())
                .paidAt(payment.getPaidAt())
                .refundAmount(payment.getRefundAmount())
                .build();
    }

    // Additional methods for PaymentController
    public java.util.List<Payment> getUserPayments(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return paymentRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    public Payment getPaymentByCode(String paymentCode) {
        return paymentRepository.findByPaymentCode(paymentCode)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    public void handleStripeWebhook(String payload, String sigHeader) {
        // Webhook handling implementation
        log.info("Received Stripe webhook: {}", payload);
        // In production, verify webhook signature and handle events
    }

    public java.util.Map<String, Object> getDailyRevenue() {
        java.time.LocalDate today = java.time.LocalDate.now();
        Double revenue = paymentRepository.getTotalRevenueForDate(today);

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("date", today);
        result.put("revenue", revenue != null ? revenue : 0.0);
        return result;
    }

    public java.util.Map<String, Object> getMonthlyRevenue() {
        java.time.LocalDate now = java.time.LocalDate.now();
        Double revenue = paymentRepository.getTotalRevenueForMonth(now.getMonthValue(), now.getYear());

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("month", now.getMonthValue());
        result.put("year", now.getYear());
        result.put("revenue", revenue != null ? revenue : 0.0);
        return result;
    }

    // Helper class for payment calculations
    @lombok.Data
    @lombok.Builder
    private static class PaymentBreakdown {
        private BigDecimal subtotal;
        private BigDecimal discountAmount;
        private BigDecimal taxAmount;
        private BigDecimal processingFee;
        private BigDecimal totalAmount;
    }
}