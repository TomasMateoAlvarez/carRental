package com.example.carrental.services;

import com.example.carrental.config.TenantContext;
import com.example.carrental.dto.*;
import com.example.carrental.enums.CustomerSegment;
import com.example.carrental.enums.CustomerStatus;
import com.example.carrental.enums.ReservationStatus;
import com.example.carrental.model.Customer;
import com.example.carrental.model.Organization;
import com.example.carrental.model.Reservation;
import com.example.carrental.repository.CustomerRepository;
import com.example.carrental.repository.OrganizationRepository;
import com.example.carrental.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final ReservationRepository reservationRepository;
    private final OrganizationRepository organizationRepository;

    // Basic CRUD Operations
    public List<CustomerResponseDTO> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        return customers.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public Page<CustomerResponseDTO> getAllCustomers(Pageable pageable) {
        Page<Customer> customers = customerRepository.findAll(pageable);
        return customers.map(this::convertToResponseDTO);
    }

    public CustomerResponseDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
        return convertToResponseDTO(customer);
    }

    public CustomerResponseDTO getCustomerByCode(String customerCode) {
        Customer customer = customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new RuntimeException("Customer not found with code: " + customerCode));
        return convertToResponseDTO(customer);
    }

    public CustomerResponseDTO createCustomer(CustomerRequestDTO customerDTO) {
        // Validate organization
        Long organizationId = TenantContext.getTenantId();
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found: " + organizationId));

        // Validate unique fields
        if (customerRepository.existsByEmail(customerDTO.getEmail())) {
            throw new RuntimeException("Customer with email already exists: " + customerDTO.getEmail());
        }

        if (customerRepository.existsByLicenseNumber(customerDTO.getLicenseNumber())) {
            throw new RuntimeException("Customer with license number already exists: " + customerDTO.getLicenseNumber());
        }

        // Generate customer code
        String customerCode = generateCustomerCode();

        // Create customer
        Customer customer = Customer.builder()
                .organization(organization)
                .customerCode(customerCode)
                .firstName(customerDTO.getFirstName())
                .lastName(customerDTO.getLastName())
                .email(customerDTO.getEmail())
                .phoneNumber(customerDTO.getPhoneNumber())
                .dateOfBirth(customerDTO.getDateOfBirth())
                .licenseNumber(customerDTO.getLicenseNumber())
                .licenseExpiryDate(customerDTO.getLicenseExpiryDate())
                .licenseIssuedCountry(customerDTO.getLicenseIssuedCountry())
                .streetAddress(customerDTO.getStreetAddress())
                .city(customerDTO.getCity())
                .province(customerDTO.getProvince())
                .postalCode(customerDTO.getPostalCode())
                .country(customerDTO.getCountry())
                .emergencyContactName(customerDTO.getEmergencyContactName())
                .emergencyContactPhone(customerDTO.getEmergencyContactPhone())
                .emergencyContactRelationship(customerDTO.getEmergencyContactRelationship())
                .preferredVehicleCategory(customerDTO.getPreferredVehicleCategory())
                .preferredPickupLocation(customerDTO.getPreferredPickupLocation())
                .marketingConsent(customerDTO.getMarketingConsent())
                .newsletterSubscription(customerDTO.getNewsletterSubscription())
                .notes(customerDTO.getNotes())
                .status(CustomerStatus.ACTIVE)
                .segment(CustomerSegment.NEW)
                .build();

        customer = customerRepository.save(customer);
        log.info("Created new customer: {} for organization: {}", customer.getCustomerCode(), organization.getName());

        return convertToResponseDTO(customer);
    }

    public CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO customerDTO) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));

        // Update fields
        customer.setFirstName(customerDTO.getFirstName());
        customer.setLastName(customerDTO.getLastName());
        customer.setEmail(customerDTO.getEmail());
        customer.setPhoneNumber(customerDTO.getPhoneNumber());
        customer.setDateOfBirth(customerDTO.getDateOfBirth());
        customer.setLicenseNumber(customerDTO.getLicenseNumber());
        customer.setLicenseExpiryDate(customerDTO.getLicenseExpiryDate());
        customer.setLicenseIssuedCountry(customerDTO.getLicenseIssuedCountry());
        customer.setStreetAddress(customerDTO.getStreetAddress());
        customer.setCity(customerDTO.getCity());
        customer.setProvince(customerDTO.getProvince());
        customer.setPostalCode(customerDTO.getPostalCode());
        customer.setCountry(customerDTO.getCountry());
        customer.setEmergencyContactName(customerDTO.getEmergencyContactName());
        customer.setEmergencyContactPhone(customerDTO.getEmergencyContactPhone());
        customer.setEmergencyContactRelationship(customerDTO.getEmergencyContactRelationship());
        customer.setPreferredVehicleCategory(customerDTO.getPreferredVehicleCategory());
        customer.setPreferredPickupLocation(customerDTO.getPreferredPickupLocation());
        customer.setMarketingConsent(customerDTO.getMarketingConsent());
        customer.setNewsletterSubscription(customerDTO.getNewsletterSubscription());
        customer.setNotes(customerDTO.getNotes());

        customer = customerRepository.save(customer);
        log.info("Updated customer: {}", customer.getCustomerCode());

        return convertToResponseDTO(customer);
    }

    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));

        // Check if customer has active reservations
        List<Reservation> activeReservations = reservationRepository.findByCustomerAndStatus(customer, ReservationStatus.CONFIRMED);
        if (!activeReservations.isEmpty()) {
            throw new RuntimeException("Cannot delete customer with active reservations");
        }

        customerRepository.delete(customer);
        log.info("Deleted customer: {}", customer.getCustomerCode());
    }

    // Customer History and Analytics
    public CustomerHistoryDTO getCustomerHistory(Long customerId) {
        Customer customer = customerRepository.findByIdWithReservations(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));

        List<ReservationSummaryDTO> reservationHistory = customer.getReservations().stream()
                .map(this::convertToReservationSummary)
                .collect(Collectors.toList());

        return CustomerHistoryDTO.builder()
                .customerId(customer.getId())
                .customerCode(customer.getCustomerCode())
                .customerName(customer.getFullName())
                .totalReservations(customer.getTotalReservations())
                .totalSpent(customer.getTotalSpent())
                .averageRentalDays(customer.getAverageRentalDays())
                .lastRentalDate(customer.getLastRentalDate())
                .customerLifetimeValue(customer.getCustomerLifetimeValue())
                .segment(customer.getSegment())
                .reservationHistory(reservationHistory)
                .build();
    }

    public void updateCustomerStatistics(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));

        // Get customer reservations
        List<Reservation> reservations = reservationRepository.findByCustomer(customer);
        List<Reservation> completedReservations = reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.COMPLETED)
                .collect(Collectors.toList());

        // Calculate statistics
        int totalReservations = completedReservations.size();
        BigDecimal totalSpent = completedReservations.stream()
                .map(Reservation::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageRentalDays = completedReservations.isEmpty() ? BigDecimal.ZERO :
                BigDecimal.valueOf(completedReservations.stream()
                        .mapToInt(Reservation::getTotalDays)
                        .average()
                        .orElse(0.0));

        Optional<LocalDateTime> lastRental = completedReservations.stream()
                .map(Reservation::getEndDate)
                .map(date -> date.atStartOfDay())
                .max(LocalDateTime::compareTo);

        // Update customer
        customer.setTotalReservations(totalReservations);
        customer.setTotalSpent(totalSpent);
        customer.setAverageRentalDays(averageRentalDays);
        customer.setLastRentalDate(lastRental.orElse(null));

        // Calculate CLV (simple formula: average order value * reservations * estimated lifetime)
        BigDecimal avgOrderValue = totalReservations > 0 ?
                totalSpent.divide(BigDecimal.valueOf(totalReservations), 2, BigDecimal.ROUND_HALF_UP) :
                BigDecimal.ZERO;
        customer.setCustomerLifetimeValue(avgOrderValue.multiply(BigDecimal.valueOf(totalReservations * 2))); // Simple CLV

        // Update segment based on reservations
        updateCustomerSegment(customer);

        customerRepository.save(customer);
        log.info("Updated statistics for customer: {}", customer.getCustomerCode());
    }

    // Search and Filter Operations
    public Page<CustomerResponseDTO> searchCustomers(String searchTerm, Pageable pageable) {
        Page<Customer> customers = customerRepository.searchCustomers(searchTerm, pageable);
        return customers.map(this::convertToResponseDTO);
    }

    public List<CustomerResponseDTO> getCustomersByStatus(CustomerStatus status) {
        List<Customer> customers = customerRepository.findByStatus(status);
        return customers.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<CustomerResponseDTO> getCustomersBySegment(CustomerSegment segment) {
        List<Customer> customers = customerRepository.findBySegment(segment);
        return customers.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<CustomerResponseDTO> getCustomersWithExpiringLicenses(int daysAhead) {
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = fromDate.plusDays(daysAhead);

        List<Customer> customers = customerRepository.findCustomersWithExpiringLicense(fromDate, toDate);
        return customers.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<CustomerResponseDTO> getTopCustomers(String criteria, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Customer> customers;

        switch (criteria.toLowerCase()) {
            case "spending":
                customers = customerRepository.findTopCustomersBySpending(pageable);
                break;
            case "reservations":
                customers = customerRepository.findTopCustomersByReservations(pageable);
                break;
            case "lifetime_value":
                customers = customerRepository.findTopCustomersByLifetimeValue(pageable);
                break;
            default:
                throw new RuntimeException("Invalid criteria: " + criteria);
        }

        return customers.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // Utility Methods
    private String generateCustomerCode() {
        // Generate unique customer code like CUS-0001
        long count = customerRepository.count();
        String customerCode;
        do {
            count++;
            customerCode = String.format("CUS-%04d", count);
        } while (customerRepository.existsByCustomerCode(customerCode));

        return customerCode;
    }

    private void updateCustomerSegment(Customer customer) {
        int totalReservations = customer.getTotalReservations();
        BigDecimal totalSpent = customer.getTotalSpent();

        if (totalSpent.compareTo(BigDecimal.valueOf(5000)) > 0 || totalReservations >= 25) {
            customer.setSegment(CustomerSegment.VIP);
        } else if (totalReservations >= 11) {
            customer.setSegment(CustomerSegment.PREMIUM);
        } else if (totalReservations >= 3) {
            customer.setSegment(CustomerSegment.REGULAR);
        } else {
            customer.setSegment(CustomerSegment.NEW);
        }
    }

    // DTO Conversion Methods
    private CustomerResponseDTO convertToResponseDTO(Customer customer) {
        return CustomerResponseDTO.builder()
                .id(customer.getId())
                .customerCode(customer.getCustomerCode())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .fullName(customer.getFullName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .dateOfBirth(customer.getDateOfBirth())
                .licenseNumber(customer.getLicenseNumber())
                .licenseExpiryDate(customer.getLicenseExpiryDate())
                .licenseIssuedCountry(customer.getLicenseIssuedCountry())
                .streetAddress(customer.getStreetAddress())
                .city(customer.getCity())
                .province(customer.getProvince())
                .postalCode(customer.getPostalCode())
                .country(customer.getCountry())
                .emergencyContactName(customer.getEmergencyContactName())
                .emergencyContactPhone(customer.getEmergencyContactPhone())
                .emergencyContactRelationship(customer.getEmergencyContactRelationship())
                .status(customer.getStatus())
                .segment(customer.getSegment())
                .totalReservations(customer.getTotalReservations())
                .totalSpent(customer.getTotalSpent())
                .averageRentalDays(customer.getAverageRentalDays())
                .lastRentalDate(customer.getLastRentalDate())
                .customerLifetimeValue(customer.getCustomerLifetimeValue())
                .preferredVehicleCategory(customer.getPreferredVehicleCategory())
                .preferredPickupLocation(customer.getPreferredPickupLocation())
                .marketingConsent(customer.getMarketingConsent())
                .newsletterSubscription(customer.getNewsletterSubscription())
                .notes(customer.getNotes())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .isLicenseExpiringSoon(customer.isLicenseExpiringSoon())
                .isVipCustomer(customer.isVipCustomer())
                .build();
    }

    private ReservationSummaryDTO convertToReservationSummary(Reservation reservation) {
        return ReservationSummaryDTO.builder()
                .id(reservation.getId())
                .reservationCode(reservation.getReservationCode())
                .startDate(reservation.getStartDate())
                .endDate(reservation.getEndDate())
                .totalDays(reservation.getTotalDays())
                .totalAmount(reservation.getTotalAmount())
                .status(reservation.getStatus())
                .vehicleBrand(reservation.getVehicle().getBrand())
                .vehicleModel(reservation.getVehicle().getModel())
                .vehicleLicensePlate(reservation.getVehicle().getLicensePlate())
                .pickupLocation(reservation.getPickupLocation())
                .returnLocation(reservation.getReturnLocation())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}