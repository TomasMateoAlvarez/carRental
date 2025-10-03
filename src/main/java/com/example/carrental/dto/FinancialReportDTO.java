package com.example.carrental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialReportDTO {

    private Long tenantId;
    private String reportType;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal subscriptionFee;
    private BigDecimal usageFees;
    private BigDecimal totalRevenue;
    private Integer totalReservations;
    private String invoiceNumber;
    private LocalDateTime generatedAt;
}