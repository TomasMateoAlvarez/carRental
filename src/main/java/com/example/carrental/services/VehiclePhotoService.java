package com.example.carrental.services;

import com.example.carrental.model.VehiclePhoto;
import com.example.carrental.model.VehicleModel;
import com.example.carrental.repository.VehiclePhotoRepository;
import com.example.carrental.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehiclePhotoService {

    private final VehiclePhotoRepository vehiclePhotoRepository;
    private final VehicleRepository vehicleRepository;

    private static final String UPLOAD_DIR = "uploads/vehicle-photos/";
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".webp"};
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Transactional
    public VehiclePhoto uploadPhoto(Long vehicleId, MultipartFile file, String photoType,
                                  String description, String inspectionType, Long takenByUserId) {

        // Validate vehicle exists
        VehicleModel vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + vehicleId));

        // Validate file
        validateFile(file);

        // Check minimum photos requirement for GENERAL type
        if ("GENERAL".equals(photoType)) {
            long currentGeneralPhotos = vehiclePhotoRepository.countGeneralPhotosByVehicle(vehicle);
            if (currentGeneralPhotos >= 10) { // Limit to prevent spam
                throw new RuntimeException("Maximum number of general photos reached (10) for vehicle: " + vehicle.getLicensePlate());
            }
        }

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = vehicleId + "_" + photoType + "_" + UUID.randomUUID() + extension;

            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            // Create photo record
            VehiclePhoto photo = VehiclePhoto.builder()
                .vehicle(vehicle)
                .photoUrl("/uploads/vehicle-photos/" + filename)
                .photoType(photoType)
                .description(description)
                .inspectionType(inspectionType)
                .isPrimary(false)
                .takenAt(LocalDateTime.now())
                .takenByUserId(takenByUserId)
                .build();

            // If this is the first photo for the vehicle, make it primary
            if (vehiclePhotoRepository.findByVehicleAndIsPrimaryTrue(vehicle).isEmpty()) {
                photo.setIsPrimary(true);
            }

            VehiclePhoto savedPhoto = vehiclePhotoRepository.save(photo);
            log.info("Photo uploaded for vehicle {}: {}", vehicle.getLicensePlate(), filename);

            return savedPhoto;

        } catch (IOException e) {
            log.error("Failed to upload photo for vehicle {}: {}", vehicleId, e.getMessage());
            throw new RuntimeException("Failed to upload photo: " + e.getMessage());
        }
    }

    @Transactional
    public void setPrimaryPhoto(Long photoId, Long vehicleId) {
        VehicleModel vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        VehiclePhoto photo = vehiclePhotoRepository.findById(photoId)
            .orElseThrow(() -> new RuntimeException("Photo not found"));

        if (!photo.getVehicle().getId().equals(vehicleId)) {
            throw new RuntimeException("Photo does not belong to this vehicle");
        }

        // Remove primary flag from current primary photo
        vehiclePhotoRepository.findByVehicleAndIsPrimaryTrue(vehicle)
            .ifPresent(currentPrimary -> {
                currentPrimary.setIsPrimary(false);
                vehiclePhotoRepository.save(currentPrimary);
            });

        // Set new primary photo
        photo.setIsPrimary(true);
        vehiclePhotoRepository.save(photo);
    }

    public List<VehiclePhoto> getVehiclePhotos(Long vehicleId) {
        return vehiclePhotoRepository.findByVehicleIdOrderByCreatedAtDesc(vehicleId);
    }

    public List<VehiclePhoto> getGeneralPhotos(Long vehicleId) {
        VehicleModel vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        return vehiclePhotoRepository.findGeneralPhotosByVehicle(vehicle);
    }

    public List<VehiclePhoto> getPhotosByType(Long vehicleId, String photoType) {
        VehicleModel vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        return vehiclePhotoRepository.findByVehicleAndPhotoTypeOrderByCreatedAtDesc(vehicle, photoType);
    }

    public List<VehiclePhoto> getInspectionPhotos(Long vehicleId, String inspectionType) {
        VehicleModel vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        return vehiclePhotoRepository.findByVehicleAndInspectionTypeOrderByCreatedAtDesc(vehicle, inspectionType);
    }

    public boolean hasMinimumPhotos(Long vehicleId) {
        VehicleModel vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        long photoCount = vehiclePhotoRepository.countGeneralPhotosByVehicle(vehicle);
        return photoCount >= 5;
    }

    @Transactional
    public void deletePhoto(Long photoId) {
        VehiclePhoto photo = vehiclePhotoRepository.findById(photoId)
            .orElseThrow(() -> new RuntimeException("Photo not found"));

        try {
            // Delete physical file
            Path filePath = Paths.get("." + photo.getPhotoUrl());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }

            // Delete database record
            vehiclePhotoRepository.delete(photo);
            log.info("Photo deleted: {}", photo.getPhotoUrl());

        } catch (IOException e) {
            log.error("Failed to delete photo file: {}", e.getMessage());
            // Still delete from database even if file deletion fails
            vehiclePhotoRepository.delete(photo);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File size exceeds maximum allowed size (10MB)");
        }

        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new RuntimeException("Invalid filename");
        }

        boolean validExtension = false;
        for (String ext : ALLOWED_EXTENSIONS) {
            if (filename.toLowerCase().endsWith(ext)) {
                validExtension = true;
                break;
            }
        }

        if (!validExtension) {
            throw new RuntimeException("Invalid file type. Only JPG, JPEG, PNG, and WEBP files are allowed");
        }
    }
}