package com.example.carrental.services;

import com.example.carrental.dto.VehicleRequestDTO;
import com.example.carrental.dto.VehicleResponseDTO;
import com.example.carrental.enums.VehicleStatus;
import com.example.carrental.exception.ResourceNotFoundException;
import com.example.carrental.mapper.VehicleMapper;
import com.example.carrental.model.VehicleModel;
import com.example.carrental.repository.VehicleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;

    public VehicleService(VehicleRepository vehicleRepository, VehicleMapper vehicleMapper) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleMapper = vehicleMapper;
    }

    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> getAllVehicles() {
        log.info("Retrieving all vehicles");
        List<VehicleResponseDTO> vehicles = vehicleRepository.findAll()
                .stream()
                .map(vehicleMapper::toResponseDTO)
                .collect(Collectors.toList());
        log.info("Found {} vehicles", vehicles.size());
        return vehicles;
    }

    @Transactional(readOnly = true)
    public Page<VehicleResponseDTO> getAllVehiclesPaged(Pageable pageable) {
        log.info("Retrieving vehicles page: {}", pageable);
        return vehicleRepository.findAll(pageable)
                .map(vehicleMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public VehicleResponseDTO getVehicleById(Long id) {
        log.info("Retrieving vehicle with ID: {}", id);
        VehicleModel vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", id));
        return vehicleMapper.toResponseDTO(vehicle);
    }

    @Transactional(readOnly = true)
    public VehicleResponseDTO getVehicleByLicensePlate(String licensePlate) {
        log.info("Retrieving vehicle with license plate: {}", licensePlate);
        VehicleModel vehicle = vehicleRepository.findByLicensePlate(licensePlate.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo con matrícula: " + licensePlate));
        return vehicleMapper.toResponseDTO(vehicle);
    }

    public VehicleResponseDTO createVehicle(VehicleRequestDTO vehicleDTO) {
        log.info("Creating new vehicle with license plate: {}", vehicleDTO.getLicensePlate());

        // Check if license plate already exists
        if (vehicleRepository.existsByLicensePlate(vehicleDTO.getLicensePlate().toUpperCase())) {
            throw new IllegalArgumentException("Ya existe un vehículo con la matrícula: " + vehicleDTO.getLicensePlate());
        }

        VehicleModel vehicle = vehicleMapper.toEntity(vehicleDTO);
        VehicleModel savedVehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle created successfully with ID: {}", savedVehicle.getId());
        return vehicleMapper.toResponseDTO(savedVehicle);
    }

    public VehicleResponseDTO updateVehicle(Long id, VehicleRequestDTO vehicleDTO) {
        log.info("Updating vehicle with ID: {}", id);
        VehicleModel existingVehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", id));

        // Check license plate uniqueness if changed
        if (!existingVehicle.getLicensePlate().equalsIgnoreCase(vehicleDTO.getLicensePlate()) &&
            vehicleRepository.existsByLicensePlate(vehicleDTO.getLicensePlate().toUpperCase())) {
            throw new IllegalArgumentException("Ya existe un vehículo con la matrícula: " + vehicleDTO.getLicensePlate());
        }

        vehicleMapper.updateEntity(existingVehicle, vehicleDTO);
        VehicleModel updatedVehicle = vehicleRepository.save(existingVehicle);
        log.info("Vehicle updated successfully with ID: {}", updatedVehicle.getId());
        return vehicleMapper.toResponseDTO(updatedVehicle);
    }

    public void deleteVehicle(Long id) {
        log.info("Deleting vehicle with ID: {}", id);
        VehicleModel vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", id));

        // Business rule: can't delete rented vehicles
        if (vehicle.getStatus() == VehicleStatus.RENTED) {
            throw new IllegalStateException("No se puede eliminar un vehículo que está alquilado");
        }

        vehicleRepository.deleteById(id);
        log.info("Vehicle deleted successfully with ID: {}", id);
    }

    // Business operations
    public VehicleResponseDTO changeVehicleStatus(Long id, VehicleStatus newStatus) {
        log.info("Changing status of vehicle {} to {}", id, newStatus);
        VehicleModel vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", id));

        vehicle.changeStatus(newStatus);
        VehicleModel updatedVehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle status changed successfully");
        return vehicleMapper.toResponseDTO(updatedVehicle);
    }

    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> getAvailableVehicles() {
        log.info("Retrieving available vehicles");
        return vehicleRepository.findAvailableVehicles()
                .stream()
                .map(vehicleMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> getAvailableVehiclesByCategory(String category) {
        log.info("Retrieving available vehicles for category: {}", category);
        return vehicleRepository.findAvailableVehiclesByCategory(category)
                .stream()
                .map(vehicleMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> getAvailableVehiclesByPriceRange(BigDecimal minRate, BigDecimal maxRate) {
        log.info("Retrieving available vehicles in price range: {} - {}", minRate, maxRate);
        return vehicleRepository.findAvailableVehiclesByPriceRange(minRate, maxRate)
                .stream()
                .map(vehicleMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> getVehiclesNeedingMaintenance() {
        log.info("Retrieving vehicles needing maintenance");
        return vehicleRepository.findVehiclesNeedingMaintenance(LocalDateTime.now())
                .stream()
                .map(vehicleMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<VehicleResponseDTO> searchVehicles(String searchTerm, Pageable pageable) {
        log.info("Searching vehicles with term: {}", searchTerm);
        return vehicleRepository.searchVehicles(searchTerm, pageable)
                .map(vehicleMapper::toResponseDTO);
    }

    // Statistics
    @Transactional(readOnly = true)
    public long countVehiclesByStatus(VehicleStatus status) {
        return vehicleRepository.countByStatus(status);
    }
}