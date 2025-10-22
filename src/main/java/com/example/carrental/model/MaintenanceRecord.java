package com.example.carrental.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    @JsonBackReference
    private VehicleModel vehicle;

    @Column(name = "maintenance_type", nullable = false, length = 100)
    private String maintenanceType; // "ROUTINE", "REPAIR", "INSPECTION", "EMERGENCY"

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "service_provider", length = 200)
    private String serviceProvider; // A quien lo llevaron

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason; // Por qué lo llevaron

    @Column(name = "cost", precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(name = "mileage_at_service", nullable = false)
    private Integer mileageAtService;

    @Column(name = "next_service_mileage")
    private Integer nextServiceMileage; // Para alertas de mantenimiento

    @Column(name = "service_date", nullable = false)
    private LocalDateTime serviceDate;

    @Column(name = "completion_date")
    private LocalDateTime completionDate;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "COMPLETED"; // "SCHEDULED", "IN_PROGRESS", "COMPLETED", "CANCELLED"

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (serviceDate == null) {
            serviceDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}