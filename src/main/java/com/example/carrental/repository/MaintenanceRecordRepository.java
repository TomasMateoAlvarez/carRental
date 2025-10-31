package com.example.carrental.repository;

import com.example.carrental.model.MaintenanceRecord;
import com.example.carrental.model.VehicleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {

    List<MaintenanceRecord> findByVehicleOrderByServiceDateDesc(VehicleModel vehicle);

    @Query("SELECT mr FROM MaintenanceRecord mr WHERE mr.vehicle.id = :vehicleId ORDER BY mr.serviceDate DESC")
    List<MaintenanceRecord> findByVehicleIdOrderByServiceDateDesc(@Param("vehicleId") Long vehicleId);

    List<MaintenanceRecord> findByStatusOrderByServiceDateDesc(String status);

    List<MaintenanceRecord> findByMaintenanceTypeOrderByServiceDateDesc(String maintenanceType);

    @Query("SELECT mr FROM MaintenanceRecord mr WHERE mr.vehicle = :vehicle " +
           "AND mr.status = 'COMPLETED' ORDER BY mr.serviceDate DESC")
    List<MaintenanceRecord> findCompletedMaintenanceByVehicle(@Param("vehicle") VehicleModel vehicle);

    @Query("SELECT mr FROM MaintenanceRecord mr WHERE mr.serviceDate BETWEEN :startDate AND :endDate " +
           "ORDER BY mr.serviceDate DESC")
    List<MaintenanceRecord> findMaintenanceByDateRange(@Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate);

    @Query("SELECT mr FROM MaintenanceRecord mr WHERE mr.vehicle = :vehicle " +
           "AND mr.status = 'COMPLETED' ORDER BY mr.serviceDate DESC LIMIT 1")
    MaintenanceRecord findLastMaintenanceByVehicle(@Param("vehicle") VehicleModel vehicle);

    @Query("SELECT COUNT(mr) FROM MaintenanceRecord mr WHERE mr.vehicle = :vehicle")
    long countMaintenanceByVehicle(@Param("vehicle") VehicleModel vehicle);

    @Query("SELECT mr FROM MaintenanceRecord mr WHERE mr.createdByUserId = :userId " +
           "ORDER BY mr.serviceDate DESC")
    List<MaintenanceRecord> findMaintenanceByUser(@Param("userId") Long userId);
}