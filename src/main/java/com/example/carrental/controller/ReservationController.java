package com.example.carrental.controller;

import com.example.carrental.dto.CreateReservationRequestDTO;
import com.example.carrental.dto.ReservationResponseDTO;
import com.example.carrental.enums.ReservationStatus;
import com.example.carrental.services.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<?> createReservation(
            @Valid @RequestBody CreateReservationRequestDTO request,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            ReservationResponseDTO response = reservationService.createReservation(request, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating reservation", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to create reservation", "message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserReservations(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<ReservationResponseDTO> reservations = reservationService.getUserReservations(username);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            log.error("Error fetching user reservations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch reservations", "message", e.getMessage()));
        }
    }

    @GetMapping("/{reservationCode}")
    public ResponseEntity<?> getReservationByCode(
            @PathVariable String reservationCode,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            ReservationResponseDTO reservation = reservationService.getReservationByCode(reservationCode, username);
            return ResponseEntity.ok(reservation);
        } catch (Exception e) {
            log.error("Error fetching reservation: {}", reservationCode, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Reservation not found", "message", e.getMessage()));
        }
    }

    @PostMapping("/{reservationCode}/confirm")
    public ResponseEntity<?> confirmReservation(
            @PathVariable String reservationCode,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            ReservationResponseDTO response = reservationService.confirmReservation(reservationCode, username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error confirming reservation: {}", reservationCode, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to confirm reservation", "message", e.getMessage()));
        }
    }

    @PostMapping("/{reservationCode}/cancel")
    public ResponseEntity<?> cancelReservation(
            @PathVariable String reservationCode,
            @RequestBody(required = false) Map<String, String> requestBody,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            String reason = requestBody != null ? requestBody.getOrDefault("reason", "No reason provided") : "No reason provided";
            ReservationResponseDTO response = reservationService.cancelReservation(reservationCode, username, reason);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error cancelling reservation: {}", reservationCode, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to cancel reservation", "message", e.getMessage()));
        }
    }

    @GetMapping("/check-availability")
    public ResponseEntity<?> checkVehicleAvailability(
            @RequestParam Long vehicleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            boolean available = reservationService.isVehicleAvailable(vehicleId, startDate, endDate);
            return ResponseEntity.ok(Map.of("available", available));
        } catch (Exception e) {
            log.error("Error checking vehicle availability", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to check availability", "message", e.getMessage()));
        }
    }

    // Admin endpoints
    @GetMapping("/admin/status/{status}")
    public ResponseEntity<?> getReservationsByStatus(
            @PathVariable ReservationStatus status,
            Authentication authentication) {
        try {
            // Check if user has admin role
            String username = authentication.getName();
            List<ReservationResponseDTO> reservations = reservationService.getReservationsByStatus(status);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            log.error("Error fetching reservations by status: {}", status, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch reservations", "message", e.getMessage()));
        }
    }

    @GetMapping("/admin/pickups/{date}")
    public ResponseEntity<?> getPickupsForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<ReservationResponseDTO> pickups = reservationService.getPickupsForDate(date);
            return ResponseEntity.ok(pickups);
        } catch (Exception e) {
            log.error("Error fetching pickups for date: {}", date, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch pickups", "message", e.getMessage()));
        }
    }

    @GetMapping("/admin/returns/{date}")
    public ResponseEntity<?> getReturnsForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<ReservationResponseDTO> returns = reservationService.getReturnsForDate(date);
            return ResponseEntity.ok(returns);
        } catch (Exception e) {
            log.error("Error fetching returns for date: {}", date, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch returns", "message", e.getMessage()));
        }
    }
}