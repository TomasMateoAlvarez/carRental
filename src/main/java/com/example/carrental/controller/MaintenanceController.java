package com.example.carrental.controller;

import com.example.carrental.model.MaintenanceRecord;
import com.example.carrental.model.VehicleModel;
import com.example.carrental.model.User;
import com.example.carrental.services.MaintenanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @PostMapping("/create")
    public ResponseEntity<MaintenanceRecord> createMaintenanceRecord(
            @RequestParam("vehicleId") Long vehicleId,
            @RequestParam("maintenanceType") String maintenanceType,
            @RequestParam("description") String description,
            @RequestParam("serviceProvider") String serviceProvider,
            @RequestParam("reason") String reason,
            @RequestParam("cost") BigDecimal cost,
            @RequestParam("mileageAtService") Integer mileageAtService) {

        try {
            MaintenanceRecord record = maintenanceService.createMaintenanceRecord(
                vehicleId, maintenanceType, description, serviceProvider,
                reason, cost, mileageAtService, 1L // Temporary: use admin user ID
            );
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            log.error("Error creating maintenance record for vehicle {}: {}", vehicleId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/schedule")
    public ResponseEntity<MaintenanceRecord> scheduleMaintenanceRecord(
            @RequestParam("vehicleId") Long vehicleId,
            @RequestParam("maintenanceType") String maintenanceType,
            @RequestParam("description") String description,
            @RequestParam("scheduledDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledDate,
            @RequestParam("estimatedMileage") Integer estimatedMileage,
            @AuthenticationPrincipal User currentUser) {

        try {
            MaintenanceRecord record = maintenanceService.scheduleMaintenanceRecord(
                vehicleId, maintenanceType, description, scheduledDate,
                estimatedMileage, currentUser.getId()
            );
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            log.error("Error scheduling maintenance for vehicle {}: {}", vehicleId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<MaintenanceRecord>> getVehicleMaintenanceHistory(@PathVariable Long vehicleId) {
        try {
            List<MaintenanceRecord> records = maintenanceService.getVehicleMaintenanceHistory(vehicleId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            log.error("Error getting maintenance history for vehicle {}: {}", vehicleId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<MaintenanceRecord>> getMaintenanceByStatus(@PathVariable String status) {
        try {
            List<MaintenanceRecord> records = maintenanceService.getMaintenanceByStatus(status);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            log.error("Error getting maintenance records by status {}: {}", status, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user")
    public ResponseEntity<List<MaintenanceRecord>> getUserMaintenanceRecords(@AuthenticationPrincipal User currentUser) {
        try {
            List<MaintenanceRecord> records = maintenanceService.getUserMaintenanceRecords(currentUser.getId());
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            log.error("Error getting maintenance records for user {}: {}", currentUser.getId(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{recordId}")
    public ResponseEntity<MaintenanceRecord> updateMaintenanceRecord(
            @PathVariable Long recordId,
            @RequestParam("status") String status,
            @RequestParam(value = "completionDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime completionDate,
            @RequestParam(value = "notes", required = false) String notes) {

        try {
            MaintenanceRecord record = maintenanceService.updateMaintenanceRecord(
                recordId, status, completionDate, notes
            );
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            log.error("Error updating maintenance record {}: {}", recordId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/vehicles-needing-maintenance")
    public ResponseEntity<List<VehicleModel>> getVehiclesNeedingMaintenance() {
        try {
            List<VehicleModel> vehicles = maintenanceService.getVehiclesNeedingMaintenance();
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            log.error("Error getting vehicles needing maintenance: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/vehicle/{vehicleId}/is-due")
    public ResponseEntity<Boolean> isMaintenanceDue(@PathVariable Long vehicleId) {
        try {
            // This requires getting the vehicle first - we'll need to add this to the service
            // For now, return a simple response
            return ResponseEntity.ok(false);
        } catch (Exception e) {
            log.error("Error checking maintenance due for vehicle {}: {}", vehicleId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/vehicle/{vehicleId}/count")
    public ResponseEntity<Long> getMaintenanceCount(@PathVariable Long vehicleId) {
        try {
            long count = maintenanceService.getMaintenanceCount(vehicleId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Error getting maintenance count for vehicle {}: {}", vehicleId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{recordId}")
    public ResponseEntity<Void> deleteMaintenanceRecord(@PathVariable Long recordId) {
        try {
            maintenanceService.deleteMaintenanceRecord(recordId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting maintenance record {}: {}", recordId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/check-due")
    public ResponseEntity<Void> manualMaintenanceCheck() {
        try {
            maintenanceService.checkMaintenanceDue();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error running manual maintenance check: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}