package com.example.carrental.repository;

import com.example.carrental.enums.VehicleStatus;
import com.example.carrental.model.VehicleModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<VehicleModel, Long> {

    // Basic queries
    Optional<VehicleModel> findByLicensePlate(String licensePlate);

    List<VehicleModel> findByStatus(VehicleStatus status);

    List<VehicleModel> findByBrandAndModel(String brand, String model);

    Page<VehicleModel> findByStatus(VehicleStatus status, Pageable pageable);

    // Business queries for car rental
    @Query("SELECT v FROM VehicleModel v WHERE v.status = 'AVAILABLE'")
    List<VehicleModel> findAvailableVehicles();

    @Query("SELECT v FROM VehicleModel v WHERE v.status = 'AVAILABLE' AND v.category = :category")
    List<VehicleModel> findAvailableVehiclesByCategory(@Param("category") String category);

    @Query("SELECT v FROM VehicleModel v WHERE v.status = 'AVAILABLE' " +
           "AND v.dailyRate BETWEEN :minRate AND :maxRate")
    List<VehicleModel> findAvailableVehiclesByPriceRange(
            @Param("minRate") BigDecimal minRate,
            @Param("maxRate") BigDecimal maxRate);

    @Query("SELECT v FROM VehicleModel v WHERE v.nextMaintenanceDate <= :date")
    List<VehicleModel> findVehiclesNeedingMaintenance(@Param("date") LocalDateTime date);

    @Query("SELECT v FROM VehicleModel v WHERE v.status IN ('MAINTENANCE', 'IN_REPAIR', 'OUT_OF_SERVICE')")
    List<VehicleModel> findUnavailableVehicles();

    // Search functionality
    @Query("SELECT v FROM VehicleModel v WHERE " +
           "LOWER(v.brand) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(v.model) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(v.licensePlate) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<VehicleModel> searchVehicles(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Statistics queries
    @Query("SELECT COUNT(v) FROM VehicleModel v WHERE v.status = :status")
    long countByStatus(@Param("status") VehicleStatus status);

    @Query("SELECT v.category, COUNT(v) FROM VehicleModel v GROUP BY v.category")
    List<Object[]> countVehiclesByCategory();

    boolean existsByLicensePlate(String licensePlate);
}