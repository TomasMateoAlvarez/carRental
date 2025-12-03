package com.example.carrental.dto;

import com.example.carrental.enums.CustomerSegment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerHistoryDTO {

    private Long customerId;
    private String customerCode;
    private String customerName;

    // Statistics
    private Integer totalReservations;
    private BigDecimal totalSpent;
    private BigDecimal averageRentalDays;
    private LocalDateTime lastRentalDate;
    private BigDecimal customerLifetimeValue;
    private CustomerSegment segment;

    // Detailed reservation history
    private List<ReservationSummaryDTO> reservationHistory;

    // Additional computed analytics
    private Integer reservationsThisYear;
    private Integer reservationsThisMonth;
    private BigDecimal spentThisYear;
    private BigDecimal spentThisMonth;
    private String favoriteVehicleCategory;
    private String mostUsedPickupLocation;
    private Double averageReservationValue;
    private Integer daysSinceLastRental;
}