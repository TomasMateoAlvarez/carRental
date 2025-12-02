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

    // Multi-tenant basic queries
    @Query("SELECT v FROM VehicleModel v WHERE v.organization.id = :#{@tenantContext.getTenantId()}")
    List<VehicleModel> findAll();

    @Query("SELECT v FROM VehicleModel v WHERE v.organization.id = :#{@tenantContext.getTenantId()} AND v.id = :id")
    Optional<VehicleModel> findById(Long id);

    @Query("SELECT v FROM VehicleModel v WHERE v.organization.id = :#{@tenantContext.getTenantId()} AND v.licensePlate = :licensePlate")
    Optional<VehicleModel> findByLicensePlate(String licensePlate);

    @Query("SELECT v FROM VehicleModel v WHERE v.organization.id = :#{@tenantContext.getTenantId()} AND v.status = :status")
    List<VehicleModel> findByStatus(VehicleStatus status);

    @Query("SELECT v FROM VehicleModel v WHERE v.organization.id = :#{@tenantContext.getTenantId()} AND v.brand = :brand AND v.model = :model")
    List<VehicleModel> findByBrandAndModel(String brand, String model);

    @Query("SELECT v FROM VehicleModel v WHERE v.organization.id = :#{@tenantContext.getTenantId()} AND v.status = :status")
    Page<VehicleModel> findByStatus(VehicleStatus status, Pageable pageable);

    // Multi-tenant business queries for car rental
    @Query("SELECT v FROM VehicleModel v WHERE v.organization.id = :#{@tenantContext.getTenantId()} AND v.status = 'AVAILABLE'")
    List<VehicleModel> findAvailableVehicles();

    @Query("SELECT v FROM VehicleModel v WHERE v.organization.id = :#{@tenantContext.getTenantId()} AND v.status = 'AVAILABLE' AND v.category = :category")
    List<VehicleModel> findAvailableVehiclesByCategory(@Param("category") String category);

    @Query("SELECT v FROM VehicleModel v WHERE v.organization.id = :#{@tenantContext.getTenantId()} AND v.status = 'AVAILABLE' " +
           "AND v.dailyRate BETWEEN :minRate AND :maxRate")
    List<VehicleModel> findAvailableVehiclesByPriceRange(
            @Param("minRate") BigDecimal minRate,
            @Param("maxRate") BigDecimal maxRate);

    @Query("SELECT v FROM VehicleModel v WHERE v.organization.id = :#{@tenantContext.getTenantId()} AND v.nextMaintenanceDate <= :date")
    List<VehicleModel> findVehiclesNeedingMaintenance(@Param("date") LocalDateTime date);

    @Query("SELECT v FROM VehicleModel v WHERE v.organization.id = :#{@tenantContext.getTenantId()} AND v.status IN ('MAINTENANCE', 'IN_REPAIR', 'OUT_OF_SERVICE')")
    List<VehicleModel> findUnavailableVehicles();

    // Multi-tenant search functionality
    @Query("SELECT v FROM VehicleModel v WHERE v.organization.id = :#{@tenantContext.getTenantId()} AND (" +
           "LOWER(v.brand) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(v.model) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(v.licensePlate) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<VehicleModel> searchVehicles(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Multi-tenant statistics queries
    @Query("SELECT COUNT(v) FROM VehicleModel v WHERE v.organization.id = :#{@tenantContext.getTenantId()} AND v.status = :status")
    long countByStatus(@Param("status") VehicleStatus status);

    @Query("SELECT v.category, COUNT(v) FROM VehicleModel v WHERE v.organization.id = :#{@tenantContext.getTenantId()} GROUP BY v.category")
    List<Object[]> countVehiclesByCategory();

    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM VehicleModel v WHERE v.organization.id = :#{@tenantContext.getTenantId()} AND v.licensePlate = :licensePlate")
    boolean existsByLicensePlate(@Param("licensePlate") String licensePlate);
}