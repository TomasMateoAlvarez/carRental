package com.example.carrental.dto;

import com.example.carrental.enums.ReservationStatus;
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
public class ReservationSummaryDTO {

    private Long id;
    private String reservationCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;
    private BigDecimal totalAmount;
    private ReservationStatus status;

    // Vehicle information
    private String vehicleBrand;
    private String vehicleModel;
    private String vehicleLicensePlate;

    // Location information
    private String pickupLocation;
    private String returnLocation;

    // Audit information
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
}