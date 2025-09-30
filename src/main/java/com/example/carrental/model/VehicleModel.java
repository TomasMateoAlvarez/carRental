package com.example.carrental.model;

import com.example.carrental.enums.VehicleStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String licensePlate; // Matrícula

    @Column(nullable = false, length = 50)
    private String brand; // Marca

    @Column(nullable = false, length = 50)
    private String model; // Modelo

    @Column(name = "model_year", nullable = false)
    private Integer year; // Año

    @Column(length = 30)
    private String color; // Color

    @Column(nullable = false)
    private Integer mileage; // Kilometraje

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyRate; // Tarifa diaria

    @Column(length = 50)
    private String category; // Categoría (Economy, Compact, SUV, etc.)

    @Column(nullable = false)
    private Integer seats; // Número de asientos

    @Column(length = 20)
    private String transmission; // Manual/Automático

    @Column(length = 20)
    private String fuelType; // Gasolina/Diesel/Eléctrico/Híbrido

    @Column(columnDefinition = "TEXT")
    private String description; // Descripción adicional

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    private LocalDateTime lastMaintenanceDate;

    private LocalDateTime nextMaintenanceDate;

    // Business logic methods
    public boolean canChangeStatusTo(VehicleStatus newStatus) {
        return this.status.canTransitionTo(newStatus);
    }

    public void changeStatus(VehicleStatus newStatus) {
        if (!canChangeStatusTo(newStatus)) {
            throw new IllegalStateException(
                String.format("Cannot change status from %s to %s for vehicle %s",
                    this.status, newStatus, this.licensePlate)
            );
        }
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isAvailableForRental() {
        return this.status == VehicleStatus.AVAILABLE;
    }

    public boolean needsMaintenance() {
        return nextMaintenanceDate != null &&
               nextMaintenanceDate.isBefore(LocalDateTime.now());
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}