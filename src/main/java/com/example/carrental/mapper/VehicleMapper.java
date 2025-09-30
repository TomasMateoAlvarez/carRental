package com.example.carrental.mapper;

import com.example.carrental.dto.VehicleRequestDTO;
import com.example.carrental.dto.VehicleResponseDTO;
import com.example.carrental.model.VehicleModel;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class VehicleMapper {

    public VehicleModel toEntity(VehicleRequestDTO dto) {
        return VehicleModel.builder()
                .licensePlate(dto.getLicensePlate().toUpperCase())
                .brand(dto.getBrand())
                .model(dto.getModel())
                .year(dto.getYear())
                .color(dto.getColor())
                .mileage(dto.getMileage())
                .status(dto.getStatus())
                .dailyRate(dto.getDailyRate())
                .category(dto.getCategory())
                .seats(dto.getSeats())
                .transmission(dto.getTransmission())
                .fuelType(dto.getFuelType())
                .description(dto.getDescription())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public VehicleResponseDTO toResponseDTO(VehicleModel vehicle) {
        VehicleResponseDTO dto = new VehicleResponseDTO();
        dto.setId(vehicle.getId());
        dto.setLicensePlate(vehicle.getLicensePlate());
        dto.setBrand(vehicle.getBrand());
        dto.setModel(vehicle.getModel());
        dto.setYear(vehicle.getYear());
        dto.setColor(vehicle.getColor());
        dto.setMileage(vehicle.getMileage());
        dto.setStatus(vehicle.getStatus());
        dto.setStatusDescription(vehicle.getStatus().getDescription());
        dto.setDailyRate(vehicle.getDailyRate());
        dto.setCategory(vehicle.getCategory());
        dto.setSeats(vehicle.getSeats());
        dto.setTransmission(vehicle.getTransmission());
        dto.setFuelType(vehicle.getFuelType());
        dto.setDescription(vehicle.getDescription());
        dto.setCreatedAt(vehicle.getCreatedAt());
        dto.setUpdatedAt(vehicle.getUpdatedAt());
        dto.setLastMaintenanceDate(vehicle.getLastMaintenanceDate());
        dto.setNextMaintenanceDate(vehicle.getNextMaintenanceDate());
        dto.setNeedsMaintenance(vehicle.needsMaintenance());
        dto.setAvailableForRental(vehicle.isAvailableForRental());
        return dto;
    }

    public void updateEntity(VehicleModel vehicle, VehicleRequestDTO dto) {
        vehicle.setLicensePlate(dto.getLicensePlate().toUpperCase());
        vehicle.setBrand(dto.getBrand());
        vehicle.setModel(dto.getModel());
        vehicle.setYear(dto.getYear());
        vehicle.setColor(dto.getColor());
        vehicle.setMileage(dto.getMileage());
        vehicle.setDailyRate(dto.getDailyRate());
        vehicle.setCategory(dto.getCategory());
        vehicle.setSeats(dto.getSeats());
        vehicle.setTransmission(dto.getTransmission());
        vehicle.setFuelType(dto.getFuelType());
        vehicle.setDescription(dto.getDescription());
        vehicle.setUpdatedAt(LocalDateTime.now());

        // Status change with validation
        if (dto.getStatus() != null && dto.getStatus() != vehicle.getStatus()) {
            vehicle.changeStatus(dto.getStatus());
        }
    }
}