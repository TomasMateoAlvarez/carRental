package com.example.carrental.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_photos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehiclePhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private VehicleModel vehicle;

    // Multi-tenant relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "photo_url", nullable = false, length = 500)
    private String photoUrl;

    @Column(name = "photo_type", nullable = false, length = 50)
    private String photoType; // "GENERAL", "FRONT", "BACK", "LEFT", "RIGHT", "INTERIOR", "DAMAGE"

    @Column(name = "description")
    private String description;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    @Column(name = "taken_at", nullable = false)
    private LocalDateTime takenAt;

    @Column(name = "taken_by_user_id")
    private Long takenByUserId;

    @Column(name = "inspection_type")
    private String inspectionType; // "PICKUP", "RETURN", "MAINTENANCE", "GENERAL"

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (takenAt == null) {
            takenAt = LocalDateTime.now();
        }
    }
}