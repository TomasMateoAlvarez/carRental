package com.example.carrental.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReservationRequestDTO {

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or in the future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @FutureOrPresent(message = "End date must be today or in the future")
    private LocalDate endDate;

    @Size(max = 200, message = "Pickup location must not exceed 200 characters")
    private String pickupLocation;

    @Size(max = 200, message = "Return location must not exceed 200 characters")
    private String returnLocation;

    @Size(max = 1000, message = "Special requests must not exceed 1000 characters")
    private String specialRequests;
}