package com.example.carrental.services;

import com.example.carrental.model.MaintenanceRecord;
import com.example.carrental.model.VehicleModel;
import com.example.carrental.repository.MaintenanceRecordRepository;
import com.example.carrental.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final VehicleRepository vehicleRepository;
    private final NotificationService notificationService;

    // Maintenance interval in kilometers
    private static final int MAINTENANCE_INTERVAL_KM = 10000;

    @Transactional
    public MaintenanceRecord createMaintenanceRecord(Long vehicleId, String maintenanceType,
                                                   String description, String serviceProvider,
                                                   String reason, BigDecimal cost,
                                                   Integer mileageAtService, Long createdByUserId) {

        VehicleModel vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + vehicleId));

        // Calculate next service mileage
        Integer nextServiceMileage = null;
        if (mileageAtService != null) {
            nextServiceMileage = mileageAtService + MAINTENANCE_INTERVAL_KM;
        }

        MaintenanceRecord record = MaintenanceRecord.builder()
            .vehicle(vehicle)
            .maintenanceType(maintenanceType)
            .description(description)
            .serviceProvider(serviceProvider)
            .reason(reason)
            .cost(cost)
            .mileageAtService(mileageAtService)
            .nextServiceMileage(nextServiceMileage)
            .serviceDate(LocalDateTime.now())
            .status("COMPLETED")
            .createdByUserId(createdByUserId)
            .build();

        MaintenanceRecord savedRecord = maintenanceRecordRepository.save(record);

        // Update vehicle maintenance dates
        vehicle.setLastMaintenanceDate(LocalDateTime.now());
        vehicle.setUpdatedAt(LocalDateTime.now());
        vehicleRepository.save(vehicle);

        // Send completion notification
        notificationService.createMaintenanceCompletedNotification(vehicle, serviceProvider, description);

        log.info("Maintenance record created for vehicle {}: {}", vehicle.getLicensePlate(), description);
        return savedRecord;
    }

    @Transactional
    public MaintenanceRecord scheduleMaintenanceRecord(Long vehicleId, String maintenanceType,
                                                     String description, LocalDateTime scheduledDate,
                                                     Integer estimatedMileage, Long createdByUserId) {

        VehicleModel vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + vehicleId));

        MaintenanceRecord record = MaintenanceRecord.builder()
            .vehicle(vehicle)
            .maintenanceType(maintenanceType)
            .description(description)
            .mileageAtService(estimatedMileage)
            .serviceDate(scheduledDate)
            .status("SCHEDULED")
            .createdByUserId(createdByUserId)
            .build();

        MaintenanceRecord savedRecord = maintenanceRecordRepository.save(record);
        log.info("Maintenance scheduled for vehicle {}: {}", vehicle.getLicensePlate(), description);

        return savedRecord;
    }

    public List<MaintenanceRecord> getVehicleMaintenanceHistory(Long vehicleId) {
        return maintenanceRecordRepository.findByVehicleIdOrderByServiceDateDesc(vehicleId);
    }

    public List<MaintenanceRecord> getMaintenanceByStatus(String status) {
        return maintenanceRecordRepository.findByStatusOrderByServiceDateDesc(status);
    }

    public List<MaintenanceRecord> getUserMaintenanceRecords(Long userId) {
        return maintenanceRecordRepository.findMaintenanceByUser(userId);
    }

    @Transactional
    public MaintenanceRecord updateMaintenanceRecord(Long recordId, String status,
                                                   LocalDateTime completionDate, String notes) {
        MaintenanceRecord record = maintenanceRecordRepository.findById(recordId)
            .orElseThrow(() -> new RuntimeException("Maintenance record not found"));

        record.setStatus(status);
        if (completionDate != null) {
            record.setCompletionDate(completionDate);
        }
        if (notes != null) {
            record.setNotes(notes);
        }

        return maintenanceRecordRepository.save(record);
    }

    // Scheduled task to check for vehicles needing maintenance
    @Scheduled(cron = "0 0 8 * * *") // Run daily at 8 AM
    @Transactional
    public void checkMaintenanceDue() {
        log.info("Running daily maintenance check...");

        List<VehicleModel> allVehicles = vehicleRepository.findAll();
        int alertsSent = 0;

        for (VehicleModel vehicle : allVehicles) {
            if (isMaintenanceDue(vehicle)) {
                String reason = determineMaintenanceReason(vehicle);
                notificationService.createMaintenanceAlert(vehicle, reason, vehicle.getMileage());
                alertsSent++;
            }
        }

        if (alertsSent > 0) {
            log.info("Sent {} maintenance alerts", alertsSent);
        } else {
            log.info("No maintenance alerts needed");
        }
    }

    public boolean isMaintenanceDue(VehicleModel vehicle) {
        // Check by mileage
        MaintenanceRecord lastMaintenance = maintenanceRecordRepository.findLastMaintenanceByVehicle(vehicle);

        if (lastMaintenance == null) {
            // No maintenance history - check if vehicle has high mileage
            return vehicle.getMileage() >= MAINTENANCE_INTERVAL_KM;
        }

        // Check if it's been 10,000 km since last maintenance
        int kmSinceLastMaintenance = vehicle.getMileage() - lastMaintenance.getMileageAtService();
        return kmSinceLastMaintenance >= MAINTENANCE_INTERVAL_KM;
    }

    private String determineMaintenanceReason(VehicleModel vehicle) {
        MaintenanceRecord lastMaintenance = maintenanceRecordRepository.findLastMaintenanceByVehicle(vehicle);

        if (lastMaintenance == null) {
            return "Primer mantenimiento requerido - el vehículo ha alcanzado " +
                   String.format("%,d", vehicle.getMileage()) + " km";
        }

        int kmSinceLastMaintenance = vehicle.getMileage() - lastMaintenance.getMileageAtService();
        return "Mantenimiento programado cada 10,000 km - han pasado " +
               String.format("%,d", kmSinceLastMaintenance) + " km desde el último mantenimiento";
    }

    public List<VehicleModel> getVehiclesNeedingMaintenance() {
        return vehicleRepository.findAll().stream()
            .filter(this::isMaintenanceDue)
            .toList();
    }

    @Transactional
    public void deleteMaintenanceRecord(Long recordId) {
        MaintenanceRecord record = maintenanceRecordRepository.findById(recordId)
            .orElseThrow(() -> new RuntimeException("Maintenance record not found"));

        maintenanceRecordRepository.delete(record);
        log.info("Maintenance record deleted: {}", recordId);
    }

    public long getMaintenanceCount(Long vehicleId) {
        VehicleModel vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        return maintenanceRecordRepository.countMaintenanceByVehicle(vehicle);
    }
}