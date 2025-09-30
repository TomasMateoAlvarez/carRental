package com.example.carrental.dto;

import com.example.carrental.enums.VehicleStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VehicleResponseDTO {
    private Long id;
    private String licensePlate;
    private String brand;
    private String model;
    private Integer year;
    private String color;
    private Integer mileage;
    private VehicleStatus status;
    private String statusDescription;
    private BigDecimal dailyRate;
    private String category;
    private Integer seats;
    private String transmission;
    private String fuelType;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastMaintenanceDate;
    private LocalDateTime nextMaintenanceDate;
    private boolean needsMaintenance;
    private boolean availableForRental;
}