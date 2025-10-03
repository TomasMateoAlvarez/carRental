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
public class DashboardKPIsDTO {

    // Revenue KPIs
    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
    private BigDecimal dailyRevenue;
    private Double revenueGrowth; // percentage

    // Fleet KPIs
    private Integer totalVehicles;
    private Integer availableVehicles;
    private Integer rentedVehicles;
    private Integer maintenanceVehicles;
    private Double utilizationRate; // percentage

    // Reservation KPIs
    private Long totalReservations;
    private Long activeReservations;
    private Long pendingReservations;
    private Long completedReservations;
    private Long cancelledReservations;
    private Double cancellationRate; // percentage

    // Customer KPIs
    private Long totalCustomers;
    private Long newCustomersThisMonth;
    private Long repeatCustomers;
    private Double customerRetentionRate; // percentage

    // Performance KPIs
    private Double averageRentalDuration; // in days
    private BigDecimal averageRevenuePerRental;
    private Double bookingConversionRate; // percentage

    // Time-based metrics
    private LocalDateTime lastUpdated;
    private String period; // "daily", "weekly", "monthly"
}