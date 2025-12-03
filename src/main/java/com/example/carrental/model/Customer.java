package com.example.carrental.model;

import com.example.carrental.enums.CustomerSegment;
import com.example.carrental.enums.CustomerStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Multi-tenant support
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    // Basic customer information
    @Column(name = "customer_code", nullable = false, unique = true, length = 20)
    private String customerCode; // CUS-001, CUS-002, etc.

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    // Driver's license information
    @Column(name = "license_number", nullable = false, length = 50)
    private String licenseNumber;

    @Column(name = "license_expiry_date")
    private LocalDate licenseExpiryDate;

    @Column(name = "license_issued_country", length = 50)
    @Builder.Default
    private String licenseIssuedCountry = "Costa Rica";

    // Address information
    @Column(name = "street_address", length = 200)
    private String streetAddress;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "province", length = 100)
    private String province;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "country", length = 50)
    @Builder.Default
    private String country = "Costa Rica";

    // Emergency contact
    @Column(name = "emergency_contact_name", length = 150)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;

    @Column(name = "emergency_contact_relationship", length = 50)
    private String emergencyContactRelationship;

    // Customer status and segmentation
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CustomerStatus status = CustomerStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "segment", length = 20)
    @Builder.Default
    private CustomerSegment segment = CustomerSegment.NEW;

    // Business analytics
    @Column(name = "total_reservations")
    @Builder.Default
    private Integer totalReservations = 0;

    @Column(name = "total_spent", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalSpent = BigDecimal.ZERO;

    @Column(name = "average_rental_days", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal averageRentalDays = BigDecimal.ZERO;

    @Column(name = "last_rental_date")
    private LocalDateTime lastRentalDate;

    @Column(name = "customer_lifetime_value", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal customerLifetimeValue = BigDecimal.ZERO;

    // Preferences
    @Column(name = "preferred_vehicle_category", length = 50)
    private String preferredVehicleCategory;

    @Column(name = "preferred_pickup_location", length = 200)
    private String preferredPickupLocation;

    @Column(name = "marketing_consent")
    @Builder.Default
    private Boolean marketingConsent = false;

    @Column(name = "newsletter_subscription")
    @Builder.Default
    private Boolean newsletterSubscription = false;

    // Payment information
    @Column(name = "stripe_customer_id", length = 100)
    private String stripeCustomerId;

    @Column(name = "preferred_payment_method", length = 50)
    private String preferredPaymentMethod;

    // Notes and additional information
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "internal_notes", columnDefinition = "TEXT")
    private String internalNotes; // Staff notes not visible to customer

    // Audit fields
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "updated_by_user_id")
    private Long updatedByUserId;

    // Relationship to reservations (for easy access to history)
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reservation> reservations;

    // Computed properties
    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Transient
    public boolean isLicenseExpiringSoon() {
        if (licenseExpiryDate == null) return false;
        return licenseExpiryDate.isBefore(LocalDate.now().plusMonths(3));
    }

    @Transient
    public boolean isVipCustomer() {
        return segment == CustomerSegment.VIP;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}