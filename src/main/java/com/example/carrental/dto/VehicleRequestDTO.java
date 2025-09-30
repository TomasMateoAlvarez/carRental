package com.example.carrental.dto;

import com.example.carrental.enums.VehicleStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VehicleRequestDTO {

    @NotBlank(message = "La matrícula es obligatoria")
    @Size(min = 6, max = 20, message = "La matrícula debe tener entre 6 y 20 caracteres")
    private String licensePlate;

    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 50, message = "La marca no puede exceder 50 caracteres")
    private String brand;

    @NotBlank(message = "El modelo es obligatorio")
    @Size(max = 50, message = "El modelo no puede exceder 50 caracteres")
    private String model;

    @NotNull(message = "El año es obligatorio")
    @Min(value = 1990, message = "El año debe ser mayor a 1990")
    @Max(value = 2030, message = "El año no puede ser mayor a 2030")
    private Integer year;

    @Size(max = 30, message = "El color no puede exceder 30 caracteres")
    private String color;

    @NotNull(message = "El kilometraje es obligatorio")
    @Min(value = 0, message = "El kilometraje no puede ser negativo")
    private Integer mileage;

    private VehicleStatus status = VehicleStatus.AVAILABLE;

    @NotNull(message = "La tarifa diaria es obligatoria")
    @DecimalMin(value = "0.01", message = "La tarifa diaria debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "Formato de tarifa inválido")
    private BigDecimal dailyRate;

    @Size(max = 50, message = "La categoría no puede exceder 50 caracteres")
    private String category;

    @NotNull(message = "El número de asientos es obligatorio")
    @Min(value = 1, message = "Debe tener al menos 1 asiento")
    @Max(value = 12, message = "No puede tener más de 12 asientos")
    private Integer seats;

    @Size(max = 20, message = "La transmisión no puede exceder 20 caracteres")
    private String transmission;

    @Size(max = 20, message = "El tipo de combustible no puede exceder 20 caracteres")
    private String fuelType;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String description;
}