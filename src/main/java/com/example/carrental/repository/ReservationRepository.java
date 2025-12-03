package com.example.carrental.repository;

import com.example.carrental.enums.ReservationStatus;
import com.example.carrental.model.Reservation;
import com.example.carrental.model.User;
import com.example.carrental.model.VehicleModel;
import com.example.carrental.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByReservationCode(String reservationCode);

    List<Reservation> findByUserOrderByCreatedAtDesc(User user);

    List<Reservation> findByStatusOrderByCreatedAtDesc(ReservationStatus status);

    List<Reservation> findByUserAndStatusOrderByCreatedAtDesc(User user, ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.vehicle = :vehicle " +
           "AND r.status IN ('CONFIRMED', 'IN_PROGRESS') " +
           "AND ((r.startDate <= :endDate AND r.endDate >= :startDate))")
    List<Reservation> findConflictingReservations(
        @Param("vehicle") VehicleModel vehicle,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT r FROM Reservation r WHERE r.status = 'CONFIRMED' " +
           "AND r.startDate = :date")
    List<Reservation> findPickupsForDate(@Param("date") LocalDate date);

    @Query("SELECT r FROM Reservation r WHERE r.status = 'IN_PROGRESS' " +
           "AND r.endDate = :date")
    List<Reservation> findReturnsForDate(@Param("date") LocalDate date);

    @Query("SELECT r FROM Reservation r WHERE r.status = 'CONFIRMED' " +
           "AND r.endDate < :date")
    List<Reservation> findOverdueReservations(@Param("date") LocalDate date);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.user = :user " +
           "AND r.status IN ('PENDING', 'CONFIRMED', 'IN_PROGRESS')")
    long countActiveReservationsByUser(@Param("user") User user);

    // Customer-related queries
    List<Reservation> findByCustomerOrderByCreatedAtDesc(Customer customer);

    List<Reservation> findByCustomer(Customer customer);

    List<Reservation> findByCustomerAndStatus(Customer customer, ReservationStatus status);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.customer = :customer " +
           "AND r.status IN ('PENDING', 'CONFIRMED', 'IN_PROGRESS')")
    long countActiveReservationsByCustomer(@Param("customer") Customer customer);

    @Query("SELECT r FROM Reservation r WHERE r.createdAt >= :startDate " +
           "AND r.createdAt < :endDate ORDER BY r.createdAt DESC")
    List<Reservation> findReservationsByDateRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT r FROM Reservation r WHERE r.vehicle = :vehicle " +
           "AND r.status IN ('CONFIRMED', 'IN_PROGRESS') " +
           "ORDER BY r.startDate")
    List<Reservation> findActiveReservationsByVehicle(@Param("vehicle") VehicleModel vehicle);

    boolean existsByReservationCode(String reservationCode);

    // Additional method for getting all reservations ordered by creation date
    List<Reservation> findAllByOrderByCreatedAtDesc();

    // Count reservations by status for dashboard KPIs
    long countByStatus(ReservationStatus status);

    // Method for vehicle status updates based on dates
    @Query("SELECT r FROM Reservation r WHERE r.status IN ('CONFIRMED', 'IN_PROGRESS') " +
           "AND (r.startDate <= :today OR r.endDate >= :today) " +
           "ORDER BY r.startDate")
    List<Reservation> findActiveReservationsForStatusUpdate(@Param("today") LocalDate today);

    // Additional methods for analytics and reporting - DISABLED multi-tenant functionality
    // @Query("SELECT r FROM Reservation r WHERE r.tenant = :tenant " +
    //        "AND DATE(r.startDate) BETWEEN :startDate AND :endDate")
    // List<Reservation> findByTenantAndDateRange(@Param("tenant") com.example.carrental.model.Tenant tenant,
    //                                          @Param("startDate") LocalDate startDate,
    //                                          @Param("endDate") LocalDate endDate);
}