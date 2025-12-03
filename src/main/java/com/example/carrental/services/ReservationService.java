package com.example.carrental.services;

import com.example.carrental.dto.CreateReservationRequestDTO;
import com.example.carrental.dto.ReservationResponseDTO;
import com.example.carrental.enums.ReservationStatus;
import com.example.carrental.enums.VehicleStatus;
import com.example.carrental.model.Customer;
import com.example.carrental.model.Reservation;
import com.example.carrental.model.User;
import com.example.carrental.model.VehicleModel;
import com.example.carrental.repository.CustomerRepository;
import com.example.carrental.repository.ReservationRepository;
import com.example.carrental.repository.UserRepository;
import com.example.carrental.repository.VehicleRepository;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    public ReservationResponseDTO createReservation(CreateReservationRequestDTO request, String username) {
        log.info("Creating reservation for user: {} and vehicle: {}", username, request.getVehicleId());

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("End date must be after start date");
        }

        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Start date cannot be in the past");
        }

        // Get user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get vehicle
        VehicleModel vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        // Get customer
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Check if vehicle is available
        if (!vehicle.isAvailableForRental()) {
            throw new RuntimeException("Vehicle is not available for rental");
        }

        // Check for conflicting reservations
        List<Reservation> conflicts = reservationRepository.findConflictingReservations(
                vehicle, request.getStartDate(), request.getEndDate());
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Vehicle is not available for the selected dates");
        }

        // Check user's active reservations limit (max 3 active reservations)
        long activeReservations = reservationRepository.countActiveReservationsByUser(user);
        if (activeReservations >= 3) {
            throw new RuntimeException("Maximum number of active reservations reached (3)");
        }

        // Create reservation
        Reservation reservation = Reservation.builder()
                .user(user)
                .customer(customer)
                .vehicle(vehicle)
                .organization(user.getOrganization()) // Set organization from user
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .pickupLocation(request.getPickupLocation())
                .returnLocation(request.getReturnLocation())
                .specialRequests(request.getSpecialRequests())
                .dailyRate(vehicle.getDailyRate())
                .status(ReservationStatus.PENDING)
                .build();

        // Calculate total amount
        reservation.calculateTotalAmount();

        // Save reservation
        reservation = reservationRepository.save(reservation);

        // Update customer statistics
        updateCustomerReservationStats(customer);

        // Update vehicle status based on reservation dates
        updateVehicleStatusBasedOnDates(vehicle, reservation);

        log.info("Reservation created successfully: {} - Vehicle {} status updated based on dates",
                reservation.getReservationCode(), vehicle.getLicensePlate());

        return mapToResponseDTO(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getUserReservations(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Reservation> reservations = reservationRepository.findByUserOrderByCreatedAtDesc(user);
        return reservations.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReservationResponseDTO getReservationByCode(String reservationCode, String username) {
        Reservation reservation = reservationRepository.findByReservationCode(reservationCode)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        // Check if user owns this reservation or is admin
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!reservation.getUser().equals(user) && !user.hasRole("ADMIN")) {
            throw new RuntimeException("Access denied");
        }

        return mapToResponseDTO(reservation);
    }

    public ReservationResponseDTO confirmReservation(String reservationCode, String username) {
        log.info("Confirming reservation: {} by user: {}", reservationCode, username);

        Reservation reservation = reservationRepository.findByReservationCode(reservationCode)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        // Check authorization (user owns reservation or is admin)
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!reservation.getUser().equals(user) && !user.hasRole("ADMIN")) {
            throw new RuntimeException("Access denied");
        }

        // Validate current status
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new RuntimeException("Reservation cannot be confirmed in current status: " + reservation.getStatus());
        }

        // Check if vehicle is still available
        if (!reservation.getVehicle().isAvailableForRental()) {
            throw new RuntimeException("Vehicle is no longer available");
        }

        // Confirm reservation
        reservation.confirm();
        reservation = reservationRepository.save(reservation);

        log.info("Reservation confirmed successfully: {}", reservationCode);

        return mapToResponseDTO(reservation);
    }

    public ReservationResponseDTO cancelReservation(String reservationCode, String username, String reason) {
        log.info("Cancelling reservation: {} by user: {}", reservationCode, username);

        Reservation reservation = reservationRepository.findByReservationCode(reservationCode)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        // Check authorization
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!reservation.getUser().equals(user) && !user.hasRole("ADMIN")) {
            throw new RuntimeException("Access denied");
        }

        // Validate current status
        if (reservation.getStatus().isFinalState()) {
            throw new RuntimeException("Reservation cannot be cancelled in current status: " + reservation.getStatus());
        }

        // Cancel reservation
        reservation.cancel(reason);
        reservation = reservationRepository.save(reservation);

        // Release vehicle back to AVAILABLE
        VehicleModel vehicle = reservation.getVehicle();
        if (vehicle.getStatus() == VehicleStatus.RESERVED) {
            vehicle.setStatus(VehicleStatus.AVAILABLE);
            vehicleRepository.save(vehicle);
            log.info("Vehicle {} released back to AVAILABLE status", vehicle.getLicensePlate());
        }

        log.info("Reservation cancelled successfully: {}", reservationCode);

        return mapToResponseDTO(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getReservationsByStatus(ReservationStatus status) {
        List<Reservation> reservations = reservationRepository.findByStatusOrderByCreatedAtDesc(status);
        return reservations.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getPickupsForDate(LocalDate date) {
        List<Reservation> reservations = reservationRepository.findPickupsForDate(date);
        return reservations.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getReturnsForDate(LocalDate date) {
        List<Reservation> reservations = reservationRepository.findReturnsForDate(date);
        return reservations.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isVehicleAvailable(Long vehicleId, LocalDate startDate, LocalDate endDate) {
        VehicleModel vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        if (!vehicle.isAvailableForRental()) {
            return false;
        }

        List<Reservation> conflicts = reservationRepository.findConflictingReservations(
                vehicle, startDate, endDate);
        return conflicts.isEmpty();
    }

    private ReservationResponseDTO mapToResponseDTO(Reservation reservation) {
        ReservationResponseDTO.ReservationResponseDTOBuilder builder = ReservationResponseDTO.builder()
                .id(reservation.getId())
                .reservationCode(reservation.getReservationCode())
                .startDate(reservation.getStartDate())
                .endDate(reservation.getEndDate())
                .pickupLocation(reservation.getPickupLocation())
                .returnLocation(reservation.getReturnLocation())
                .status(reservation.getStatus())
                .dailyRate(reservation.getDailyRate())
                .totalDays(reservation.getTotalDays())
                .totalAmount(reservation.getTotalAmount())
                .specialRequests(reservation.getSpecialRequests())
                .createdAt(reservation.getCreatedAt())
                .confirmedAt(reservation.getConfirmedAt())
                .vehicleId(reservation.getVehicle().getId())
                .vehicleBrand(reservation.getVehicle().getBrand())
                .vehicleModel(reservation.getVehicle().getModel())
                .vehicleLicensePlate(reservation.getVehicle().getLicensePlate())
                .vehicleCategory(reservation.getVehicle().getCategory())
                .userId(reservation.getUser().getId())
                .userFullName(reservation.getUser().getFullName())
                .userEmail(reservation.getUser().getEmail());

        // Add customer information if exists
        if (reservation.getCustomer() != null) {
            builder.customerId(reservation.getCustomer().getId())
                    .customerCode(reservation.getCustomer().getCustomerCode())
                    .customerFullName(reservation.getCustomer().getFullName())
                    .customerEmail(reservation.getCustomer().getEmail())
                    .customerPhone(reservation.getCustomer().getPhoneNumber());
        }

        // Add rental information if exists
        if (reservation.getRental() != null) {
            builder.rentalId(reservation.getRental().getId())
                    .rentalCode(reservation.getRental().getRentalCode())
                    .pickupDateTime(reservation.getRental().getPickupDateTime())
                    .expectedReturnDateTime(reservation.getRental().getExpectedReturnDateTime())
                    .actualReturnDateTime(reservation.getRental().getActualReturnDateTime());
        }

        return builder.build();
    }

    // Additional methods for frontend compatibility
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAllByOrderByCreatedAtDesc();
        return reservations.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public ReservationResponseDTO updateReservationStatus(Long id, ReservationStatus status) {
        log.info("Updating reservation {} to status {}", id, status);

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        // Validate status transition
        validateStatusTransition(reservation.getStatus(), status);

        // Update status
        reservation.setStatus(status);

        // Set specific timestamps based on status
        switch (status) {
            case CONFIRMED:
                reservation.confirm();
                break;
            case CANCELLED:
                reservation.cancel("Status updated by admin");
                break;
            default:
                // For other statuses, just update
                break;
        }

        reservation = reservationRepository.save(reservation);
        log.info("Reservation {} status updated to {}", id, status);

        return mapToResponseDTO(reservation);
    }

    public void deleteReservation(Long id, String username) {
        log.info("Deleting reservation {} by user {}", id, username);

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        // Check authorization
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!reservation.getUser().equals(user) && !user.hasRole("ADMIN")) {
            throw new RuntimeException("Access denied");
        }

        // Validate that reservation can be deleted
        if (reservation.getStatus().isFinalState() && reservation.getStatus() != ReservationStatus.CANCELLED) {
            throw new RuntimeException("Cannot delete reservation in status: " + reservation.getStatus());
        }

        reservationRepository.delete(reservation);
        log.info("Reservation {} deleted successfully", id);
    }

    private void validateStatusTransition(ReservationStatus from, ReservationStatus to) {
        // Define valid transitions
        switch (from) {
            case PENDING:
                if (to != ReservationStatus.CONFIRMED && to != ReservationStatus.CANCELLED) {
                    throw new RuntimeException("Invalid status transition from " + from + " to " + to);
                }
                break;
            case CONFIRMED:
                if (to != ReservationStatus.IN_PROGRESS && to != ReservationStatus.CANCELLED && to != ReservationStatus.NO_SHOW) {
                    throw new RuntimeException("Invalid status transition from " + from + " to " + to);
                }
                break;
            case IN_PROGRESS:
                if (to != ReservationStatus.COMPLETED) {
                    throw new RuntimeException("Invalid status transition from " + from + " to " + to);
                }
                break;
            case COMPLETED:
            case CANCELLED:
            case NO_SHOW:
                throw new RuntimeException("Cannot change status from final state: " + from);
            default:
                throw new RuntimeException("Unknown status: " + from);
        }
    }

    /**
     * Update customer statistics after reservation creation or status change
     */
    private void updateCustomerReservationStats(Customer customer) {
        log.debug("Updating reservation statistics for customer: {}", customer.getCustomerCode());

        // Get all reservations for this customer
        List<Reservation> customerReservations = reservationRepository.findByCustomerOrderByCreatedAtDesc(customer);

        // Calculate statistics
        int totalReservations = customerReservations.size();
        BigDecimal totalSpent = customerReservations.stream()
                .filter(res -> res.getStatus() == ReservationStatus.COMPLETED)
                .map(Reservation::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate average rental days
        double averageRentalDays = customerReservations.stream()
                .filter(res -> res.getStatus() == ReservationStatus.COMPLETED)
                .mapToInt(Reservation::getTotalDays)
                .average()
                .orElse(0.0);

        // Find last rental date
        LocalDateTime lastRentalDate = customerReservations.stream()
                .filter(res -> res.getStatus() == ReservationStatus.COMPLETED)
                .map(Reservation::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        // Calculate customer lifetime value (simple calculation: total spent)
        BigDecimal customerLifetimeValue = totalSpent;

        // Update customer statistics
        customer.setTotalReservations(totalReservations);
        customer.setTotalSpent(totalSpent);
        customer.setAverageRentalDays(BigDecimal.valueOf(averageRentalDays));
        customer.setLastRentalDate(lastRentalDate);
        customer.setCustomerLifetimeValue(customerLifetimeValue);

        // Update customer segment based on reservations
        if (totalReservations == 0) {
            customer.setSegment(com.example.carrental.enums.CustomerSegment.NEW);
        } else if (totalReservations <= 3) {
            customer.setSegment(com.example.carrental.enums.CustomerSegment.REGULAR);
        } else if (totalReservations <= 10) {
            customer.setSegment(com.example.carrental.enums.CustomerSegment.PREMIUM);
        } else {
            customer.setSegment(com.example.carrental.enums.CustomerSegment.VIP);
        }

        // Save updated customer
        customerRepository.save(customer);

        log.debug("Customer {} statistics updated: {} reservations, {} total spent",
                customer.getCustomerCode(), totalReservations, totalSpent);
    }

    /**
     * Update vehicle status based on reservation dates
     * Only set to RESERVED if reservation is active today
     */
    private void updateVehicleStatusBasedOnDates(VehicleModel vehicle, Reservation reservation) {
        LocalDate today = LocalDate.now();

        // Only reserve the vehicle if the reservation starts today or is currently active
        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            if (isDateInRange(today, reservation.getStartDate(), reservation.getEndDate())) {
                vehicle.setStatus(VehicleStatus.RESERVED);
                log.info("Vehicle {} set to RESERVED for active reservation {}",
                    vehicle.getLicensePlate(), reservation.getReservationCode());
            } else {
                // Reservation is confirmed but not active yet - keep vehicle available
                log.info("Vehicle {} kept AVAILABLE - reservation {} not yet active",
                    vehicle.getLicensePlate(), reservation.getReservationCode());
            }
        } else {
            // Pending reservation - don't block the vehicle yet
            log.info("Vehicle {} kept AVAILABLE - reservation {} is still pending",
                vehicle.getLicensePlate(), reservation.getReservationCode());
        }

        vehicleRepository.save(vehicle);
    }

    /**
     * Check if a date is within a range (inclusive)
     */
    private boolean isDateInRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Scheduled task to update vehicle statuses based on current date
     * Should be called daily to release vehicles automatically
     */
    public void updateAllVehicleStatusesBasedOnDates() {
        log.info("Starting automatic vehicle status update based on current date");

        LocalDate today = LocalDate.now();

        // Get all reservations that might affect vehicle status
        List<Reservation> activeReservations = reservationRepository
            .findActiveReservationsForStatusUpdate(today);

        for (Reservation reservation : activeReservations) {
            VehicleModel vehicle = reservation.getVehicle();

            if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
                if (isDateInRange(today, reservation.getStartDate(), reservation.getEndDate())) {
                    // Reservation is active - vehicle should be reserved
                    if (vehicle.getStatus() != VehicleStatus.RESERVED) {
                        vehicle.setStatus(VehicleStatus.RESERVED);
                        vehicleRepository.save(vehicle);
                        log.info("Vehicle {} automatically set to RESERVED for active reservation {}",
                            vehicle.getLicensePlate(), reservation.getReservationCode());
                    }
                } else if (today.isAfter(reservation.getEndDate())) {
                    // Reservation has ended - vehicle should be available
                    if (vehicle.getStatus() == VehicleStatus.RESERVED) {
                        vehicle.setStatus(VehicleStatus.AVAILABLE);
                        vehicleRepository.save(vehicle);
                        log.info("Vehicle {} automatically released to AVAILABLE - reservation {} ended",
                            vehicle.getLicensePlate(), reservation.getReservationCode());
                    }
                }
            }
        }

        log.info("Automatic vehicle status update completed");
    }
}