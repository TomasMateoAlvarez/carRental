package com.example.carrental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequestDTO {

    @NotNull(message = "Payment ID is required")
    private Long paymentId;

    private BigDecimal amount; // If null, refund full amount

    @NotNull(message = "Reason is required")
    private String reason;

    private String description;
}