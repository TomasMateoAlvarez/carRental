package com.example.carrental.repository;

import com.example.carrental.enums.RentalStatus;
import com.example.carrental.model.Rental;
import com.example.carrental.model.Reservation;
import com.example.carrental.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {

    Optional<Rental> findByRentalCode(String rentalCode);

    Optional<Rental> findByReservation(Reservation reservation);

    List<Rental> findByStatusOrderByCreatedAtDesc(RentalStatus status);

    @Query("SELECT r FROM Rental r WHERE r.status = 'ACTIVE' " +
           "AND r.expectedReturnDateTime < :currentTime")
    List<Rental> findOverdueRentals(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT r FROM Rental r WHERE r.reservation.user = :user " +
           "ORDER BY r.createdAt DESC")
    List<Rental> findByUserOrderByCreatedAtDesc(@Param("user") User user);

    @Query("SELECT r FROM Rental r WHERE r.pickupEmployee = :employee " +
           "OR r.returnEmployee = :employee ORDER BY r.createdAt DESC")
    List<Rental> findByEmployeeOrderByCreatedAtDesc(@Param("employee") User employee);

    @Query("SELECT r FROM Rental r WHERE r.pickupDateTime >= :startTime " +
           "AND r.pickupDateTime < :endTime ORDER BY r.pickupDateTime")
    List<Rental> findPickupsByDateRange(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT r FROM Rental r WHERE r.actualReturnDateTime >= :startTime " +
           "AND r.actualReturnDateTime < :endTime ORDER BY r.actualReturnDateTime")
    List<Rental> findReturnsByDateRange(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT COUNT(r) FROM Rental r WHERE r.status = 'ACTIVE'")
    long countActiveRentals();

    @Query("SELECT r FROM Rental r WHERE r.reservation.vehicle.id = :vehicleId " +
           "ORDER BY r.createdAt DESC")
    List<Rental> findByVehicleIdOrderByCreatedAtDesc(@Param("vehicleId") Long vehicleId);

    boolean existsByRentalCode(String rentalCode);
}