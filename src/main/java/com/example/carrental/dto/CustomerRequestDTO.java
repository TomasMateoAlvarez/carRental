package com.example.carrental.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequestDTO {

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phoneNumber;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "License number is required")
    @Size(max = 50, message = "License number cannot exceed 50 characters")
    private String licenseNumber;

    @Future(message = "License expiry date must be in the future")
    private LocalDate licenseExpiryDate;

    @Size(max = 50, message = "License issued country cannot exceed 50 characters")
    @Builder.Default
    private String licenseIssuedCountry = "Costa Rica";

    @Size(max = 200, message = "Street address cannot exceed 200 characters")
    private String streetAddress;

    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @Size(max = 100, message = "Province cannot exceed 100 characters")
    private String province;

    @Size(max = 20, message = "Postal code cannot exceed 20 characters")
    private String postalCode;

    @Size(max = 50, message = "Country cannot exceed 50 characters")
    @Builder.Default
    private String country = "Costa Rica";

    @Size(max = 150, message = "Emergency contact name cannot exceed 150 characters")
    private String emergencyContactName;

    @Size(max = 20, message = "Emergency contact phone cannot exceed 20 characters")
    private String emergencyContactPhone;

    @Size(max = 50, message = "Emergency contact relationship cannot exceed 50 characters")
    private String emergencyContactRelationship;

    @Size(max = 50, message = "Preferred vehicle category cannot exceed 50 characters")
    private String preferredVehicleCategory;

    @Size(max = 200, message = "Preferred pickup location cannot exceed 200 characters")
    private String preferredPickupLocation;

    @Builder.Default
    private Boolean marketingConsent = false;

    @Builder.Default
    private Boolean newsletterSubscription = false;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
}