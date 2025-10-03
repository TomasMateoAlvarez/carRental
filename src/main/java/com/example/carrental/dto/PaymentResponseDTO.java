package com.example.carrental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {

    private Long paymentId;
    private String paymentCode;
    private String clientSecret; // For Stripe frontend integration
    private BigDecimal amount;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal processingFee;
    private BigDecimal refundAmount;
    private String currency;
    private String status;
    private String paymentMethod;
    private String stripePublishableKey;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;

    // Reservation details
    private Long reservationId;
    private String reservationCode;

    // User details
    private Long userId;
    private String userFullName;
    private String userEmail;

    // Additional payment details
    private String failureReason;
    private String refundReason;

    // Receipt information
    private ReceiptInfoDTO receiptInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceiptInfoDTO {
        private String receiptNumber;
        private String receiptUrl;
        private LocalDateTime issuedAt;
        private CompanyInfoDTO companyInfo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyInfoDTO {
        private String name;
        private String address;
        private String taxId;
        private String phone;
        private String email;
    }
}