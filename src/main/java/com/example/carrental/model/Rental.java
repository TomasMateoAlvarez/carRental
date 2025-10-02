package com.example.carrental.model;

import com.example.carrental.enums.RentalStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rentals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(name = "rental_code", nullable = false, unique = true, length = 20)
    private String rentalCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RentalStatus status = RentalStatus.ACTIVE;

    @Column(name = "pickup_datetime", nullable = false)
    private LocalDateTime pickupDateTime;

    @Column(name = "expected_return_datetime", nullable = false)
    private LocalDateTime expectedReturnDateTime;

    @Column(name = "actual_return_datetime")
    private LocalDateTime actualReturnDateTime;

    @Column(name = "pickup_mileage", nullable = false)
    private Integer pickupMileage;

    @Column(name = "return_mileage")
    private Integer returnMileage;

    @Column(name = "fuel_level_pickup", nullable = false)
    private String fuelLevelPickup; // Full, 3/4, 1/2, 1/4, Empty

    @Column(name = "fuel_level_return")
    private String fuelLevelReturn;

    @Column(name = "pickup_notes", columnDefinition = "TEXT")
    private String pickupNotes;

    @Column(name = "return_notes", columnDefinition = "TEXT")
    private String returnNotes;

    @Column(name = "damage_notes", columnDefinition = "TEXT")
    private String damageNotes;

    @Column(name = "additional_charges", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal additionalCharges = BigDecimal.ZERO;

    @Column(name = "late_return_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal lateReturnFee = BigDecimal.ZERO;

    @Column(name = "damage_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal damageFee = BigDecimal.ZERO;

    @Column(name = "fuel_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal fuelFee = BigDecimal.ZERO;

    @Column(name = "total_fee", precision = 12, scale = 2)
    private BigDecimal totalFee;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pickup_employee_id")
    private User pickupEmployee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_employee_id")
    private User returnEmployee;

    // Business logic methods
    public void startRental(LocalDateTime pickupTime, Integer mileage, String fuelLevel, User employee) {
        this.pickupDateTime = pickupTime;
        this.pickupMileage = mileage;
        this.fuelLevelPickup = fuelLevel;
        this.pickupEmployee = employee;
        this.status = RentalStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();

        // Update vehicle status
        if (reservation != null && reservation.getVehicle() != null) {
            reservation.getVehicle().changeStatus(com.example.carrental.enums.VehicleStatus.RENTED);
            reservation.changeStatus(com.example.carrental.enums.ReservationStatus.IN_PROGRESS);
        }
    }

    public void completeRental(LocalDateTime returnTime, Integer mileage, String fuelLevel, User employee) {
        this.actualReturnDateTime = returnTime;
        this.returnMileage = mileage;
        this.fuelLevelReturn = fuelLevel;
        this.returnEmployee = employee;
        this.status = RentalStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();

        calculateFees();

        // Update vehicle and reservation status
        if (reservation != null && reservation.getVehicle() != null) {
            reservation.getVehicle().changeStatus(com.example.carrental.enums.VehicleStatus.WASHING);
            reservation.changeStatus(com.example.carrental.enums.ReservationStatus.COMPLETED);
        }
    }

    public void calculateFees() {
        BigDecimal totalAdditional = BigDecimal.ZERO;

        // Calculate late return fee
        if (actualReturnDateTime != null && actualReturnDateTime.isAfter(expectedReturnDateTime)) {
            long hoursLate = java.time.Duration.between(expectedReturnDateTime, actualReturnDateTime).toHours();
            this.lateReturnFee = BigDecimal.valueOf(hoursLate * 10); // $10 per hour late
            totalAdditional = totalAdditional.add(lateReturnFee);
        }

        // Calculate fuel fee (if returned with less fuel)
        if (fuelLevelPickup != null && fuelLevelReturn != null) {
            int pickupLevel = fuelLevelToPercentage(fuelLevelPickup);
            int returnLevel = fuelLevelToPercentage(fuelLevelReturn);
            if (returnLevel < pickupLevel) {
                this.fuelFee = BigDecimal.valueOf((pickupLevel - returnLevel) * 2); // $2 per 25% level
                totalAdditional = totalAdditional.add(fuelFee);
            }
        }

        this.additionalCharges = totalAdditional;

        // Calculate total fee (base amount + additional charges)
        if (reservation != null) {
            this.totalFee = reservation.getTotalAmount().add(additionalCharges).add(damageFee);
        }
    }

    private int fuelLevelToPercentage(String fuelLevel) {
        return switch (fuelLevel.toLowerCase()) {
            case "full" -> 100;
            case "3/4" -> 75;
            case "1/2" -> 50;
            case "1/4" -> 25;
            case "empty" -> 0;
            default -> 0;
        };
    }

    public boolean isOverdue() {
        return status == RentalStatus.ACTIVE &&
               LocalDateTime.now().isAfter(expectedReturnDateTime);
    }

    public long getHoursOverdue() {
        if (!isOverdue()) return 0;
        return java.time.Duration.between(expectedReturnDateTime, LocalDateTime.now()).toHours();
    }

    public int getTotalMilesDriven() {
        if (pickupMileage != null && returnMileage != null) {
            return returnMileage - pickupMileage;
        }
        return 0;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (rentalCode == null) {
            generateRentalCode();
        }
    }

    private void generateRentalCode() {
        this.rentalCode = "RNT" + System.currentTimeMillis() +
                         String.valueOf((int)(Math.random() * 1000)).substring(0, 3);
    }
}