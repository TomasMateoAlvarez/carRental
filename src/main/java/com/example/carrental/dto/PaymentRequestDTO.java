package com.example.carrental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {

    @NotNull(message = "Reservation ID is required")
    private Long reservationId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;

    @DecimalMin(value = "0.0", message = "Discount amount must be positive")
    private BigDecimal discountAmount;

    private String promoCode;

    private String paymentMethodId; // Stripe payment method ID

    private String currency = "USD";

    private String description;

    // Billing address (optional but recommended)
    private BillingAddressDTO billingAddress;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillingAddressDTO {
        private String line1;
        private String line2;
        private String city;
        private String state;
        private String postalCode;
        private String country;
    }
}