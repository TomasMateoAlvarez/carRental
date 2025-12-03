package com.example.carrental.dto;

import com.example.carrental.enums.CustomerSegment;
import com.example.carrental.enums.CustomerStatus;
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
public class CustomerResponseDTO {

    private Long id;
    private String customerCode;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;

    // License information
    private String licenseNumber;
    private LocalDate licenseExpiryDate;
    private String licenseIssuedCountry;

    // Address information
    private String streetAddress;
    private String city;
    private String province;
    private String postalCode;
    private String country;

    // Emergency contact
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelationship;

    // Customer status and segmentation
    private CustomerStatus status;
    private CustomerSegment segment;

    // Business analytics
    private Integer totalReservations;
    private BigDecimal totalSpent;
    private BigDecimal averageRentalDays;
    private LocalDateTime lastRentalDate;
    private BigDecimal customerLifetimeValue;

    // Preferences
    private String preferredVehicleCategory;
    private String preferredPickupLocation;
    private Boolean marketingConsent;
    private Boolean newsletterSubscription;

    // Additional information
    private String notes;

    // Audit information
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Computed properties
    private Boolean isLicenseExpiringSoon;
    private Boolean isVipCustomer;
}