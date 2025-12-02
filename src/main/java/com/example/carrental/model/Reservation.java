package com.example.carrental.model;

import com.example.carrental.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_code", nullable = false, unique = true, length = 20)
    private String reservationCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private VehicleModel vehicle;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "pickup_location", length = 200)
    private String pickupLocation;

    @Column(name = "return_location", length = 200)
    private String returnLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING;

    @Column(name = "daily_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyRate;

    @Column(name = "total_days", nullable = false)
    private Integer totalDays;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "deposit_amount", precision = 10, scale = 2)
    private BigDecimal depositAmount;

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Rental rental;

    // Multi-tenant relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    // Business logic methods
    public void calculateTotalAmount() {
        if (startDate != null && endDate != null && dailyRate != null) {
            this.totalDays = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
            this.totalAmount = dailyRate.multiply(BigDecimal.valueOf(totalDays));
        }
    }

    public boolean canTransitionTo(ReservationStatus newStatus) {
        return this.status.canTransitionTo(newStatus);
    }

    public void changeStatus(ReservationStatus newStatus) {
        if (!canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                String.format("Cannot change reservation status from %s to %s for reservation %s",
                    this.status, newStatus, this.reservationCode)
            );
        }
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();

        // Set timestamps for specific status changes
        switch (newStatus) {
            case CONFIRMED -> this.confirmedAt = LocalDateTime.now();
            case CANCELLED -> this.cancelledAt = LocalDateTime.now();
        }
    }

    public void confirm() {
        changeStatus(ReservationStatus.CONFIRMED);
        if (vehicle != null) {
            vehicle.changeStatus(com.example.carrental.enums.VehicleStatus.RESERVED);
        }
    }

    public void cancel(String reason) {
        changeStatus(ReservationStatus.CANCELLED);
        this.cancellationReason = reason;
        if (vehicle != null && vehicle.getStatus() == com.example.carrental.enums.VehicleStatus.RESERVED) {
            vehicle.changeStatus(com.example.carrental.enums.VehicleStatus.AVAILABLE);
        }
    }

    public boolean isActive() {
        return status == ReservationStatus.CONFIRMED || status == ReservationStatus.PENDING;
    }

    public boolean isInProgress() {
        LocalDate today = LocalDate.now();
        return status == ReservationStatus.CONFIRMED &&
               !startDate.isAfter(today) &&
               !endDate.isBefore(today);
    }

    public boolean isOverdue() {
        return status == ReservationStatus.CONFIRMED &&
               LocalDate.now().isAfter(endDate);
    }

    public long getDaysUntilStart() {
        return ChronoUnit.DAYS.between(LocalDate.now(), startDate);
    }

    public long getDaysUntilEnd() {
        return ChronoUnit.DAYS.between(LocalDate.now(), endDate);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        calculateTotalAmount();
        if (reservationCode == null) {
            generateReservationCode();
        }
    }

    private void generateReservationCode() {
        this.reservationCode = "RES" + System.currentTimeMillis() +
                              String.valueOf((int)(Math.random() * 1000)).substring(0, 3);
    }
}