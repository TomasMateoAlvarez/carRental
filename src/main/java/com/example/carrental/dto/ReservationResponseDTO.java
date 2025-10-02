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
public class ReservationResponseDTO {

    private Long id;
    private String reservationCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private String pickupLocation;
    private String returnLocation;
    private ReservationStatus status;
    private BigDecimal dailyRate;
    private Integer totalDays;
    private BigDecimal totalAmount;
    private String specialRequests;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;

    // Vehicle information
    private Long vehicleId;
    private String vehicleBrand;
    private String vehicleModel;
    private String vehicleLicensePlate;
    private String vehicleCategory;

    // User information
    private Long userId;
    private String userFullName;
    private String userEmail;

    // Rental information (if exists)
    private Long rentalId;
    private String rentalCode;
    private LocalDateTime pickupDateTime;
    private LocalDateTime expectedReturnDateTime;
    private LocalDateTime actualReturnDateTime;
}