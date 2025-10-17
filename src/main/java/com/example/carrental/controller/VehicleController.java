package com.example.carrental.controller;

import com.example.carrental.dto.VehicleRequestDTO;
import com.example.carrental.dto.VehicleResponseDTO;
import com.example.carrental.enums.VehicleStatus;
import com.example.carrental.services.VehicleService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/vehicles")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public ResponseEntity<List<VehicleResponseDTO>> getAllVehicles() {
        List<VehicleResponseDTO> vehicles = vehicleService.getAllVehicles();
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<VehicleResponseDTO>> getAllVehiclesPaged(Pageable pageable) {
        Page<VehicleResponseDTO> vehicles = vehicleService.getAllVehiclesPaged(pageable);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponseDTO> getVehicleById(@PathVariable Long id) {
        VehicleResponseDTO vehicle = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(vehicle);
    }

    @GetMapping("/license-plate/{licensePlate}")
    public ResponseEntity<VehicleResponseDTO> getVehicleByLicensePlate(@PathVariable String licensePlate) {
        VehicleResponseDTO vehicle = vehicleService.getVehicleByLicensePlate(licensePlate);
        return ResponseEntity.ok(vehicle);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('VEHICLE_CREATE')")
    public ResponseEntity<VehicleResponseDTO> createVehicle(@Valid @RequestBody VehicleRequestDTO vehicleDTO) {
        VehicleResponseDTO createdVehicle = vehicleService.createVehicle(vehicleDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVehicle);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VEHICLE_UPDATE')")
    public ResponseEntity<VehicleResponseDTO> updateVehicle(@PathVariable Long id,
                                                          @Valid @RequestBody VehicleRequestDTO vehicleDTO) {
        VehicleResponseDTO updatedVehicle = vehicleService.updateVehicle(id, vehicleDTO);
        return ResponseEntity.ok(updatedVehicle);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VEHICLE_DELETE')")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }

    // Business operations - Employees can ONLY change vehicle status
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('VEHICLE_STATUS_CHANGE')")
    public ResponseEntity<VehicleResponseDTO> changeVehicleStatus(@PathVariable Long id,
                                                                @RequestParam VehicleStatus status) {
        VehicleResponseDTO updatedVehicle = vehicleService.changeVehicleStatus(id, status);
        return ResponseEntity.ok(updatedVehicle);
    }

    // Search and filter endpoints
    @GetMapping("/available")
    public ResponseEntity<List<VehicleResponseDTO>> getAvailableVehicles() {
        List<VehicleResponseDTO> vehicles = vehicleService.getAvailableVehicles();
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/available/category/{category}")
    public ResponseEntity<List<VehicleResponseDTO>> getAvailableVehiclesByCategory(@PathVariable String category) {
        List<VehicleResponseDTO> vehicles = vehicleService.getAvailableVehiclesByCategory(category);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/available/price-range")
    public ResponseEntity<List<VehicleResponseDTO>> getAvailableVehiclesByPriceRange(
            @RequestParam BigDecimal minRate,
            @RequestParam BigDecimal maxRate) {
        List<VehicleResponseDTO> vehicles = vehicleService.getAvailableVehiclesByPriceRange(minRate, maxRate);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/maintenance-needed")
    @PreAuthorize("hasPermission('MAINTENANCE_RECORD_MANAGE', 'READ')")
    public ResponseEntity<List<VehicleResponseDTO>> getVehiclesNeedingMaintenance() {
        List<VehicleResponseDTO> vehicles = vehicleService.getVehiclesNeedingMaintenance();
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<VehicleResponseDTO>> searchVehicles(@RequestParam String q, Pageable pageable) {
        Page<VehicleResponseDTO> vehicles = vehicleService.searchVehicles(q, pageable);
        return ResponseEntity.ok(vehicles);
    }

    // Statistics endpoints
    @GetMapping("/stats/count-by-status")
    public ResponseEntity<Long> countVehiclesByStatus(@RequestParam VehicleStatus status) {
        long count = vehicleService.countVehiclesByStatus(status);
        return ResponseEntity.ok(count);
    }
}