package com.example.carrental.controller;

import com.example.carrental.model.VehiclePhoto;
import com.example.carrental.model.User;
import com.example.carrental.services.VehiclePhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/vehicle-photos")
@RequiredArgsConstructor
public class VehiclePhotoController {

    private final VehiclePhotoService vehiclePhotoService;

    @PostMapping("/upload")
    @PreAuthorize("hasPermission('VEHICLE_PHOTO_UPLOAD', 'CREATE')")
    public ResponseEntity<VehiclePhoto> uploadPhoto(
            @RequestParam("vehicleId") Long vehicleId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("photoType") String photoType,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "inspectionType", required = false) String inspectionType,
            @AuthenticationPrincipal User currentUser) {

        try {
            VehiclePhoto photo = vehiclePhotoService.uploadPhoto(
                vehicleId, file, photoType, description, inspectionType, currentUser.getId()
            );
            return ResponseEntity.ok(photo);
        } catch (Exception e) {
            log.error("Error uploading photo for vehicle {}: {}", vehicleId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<VehiclePhoto>> getVehiclePhotos(@PathVariable Long vehicleId) {
        try {
            List<VehiclePhoto> photos = vehiclePhotoService.getVehiclePhotos(vehicleId);
            return ResponseEntity.ok(photos);
        } catch (Exception e) {
            log.error("Error getting photos for vehicle {}: {}", vehicleId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/vehicle/{vehicleId}/general")
    public ResponseEntity<List<VehiclePhoto>> getGeneralPhotos(@PathVariable Long vehicleId) {
        try {
            List<VehiclePhoto> photos = vehiclePhotoService.getGeneralPhotos(vehicleId);
            return ResponseEntity.ok(photos);
        } catch (Exception e) {
            log.error("Error getting general photos for vehicle {}: {}", vehicleId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/vehicle/{vehicleId}/type/{photoType}")
    public ResponseEntity<List<VehiclePhoto>> getPhotosByType(
            @PathVariable Long vehicleId,
            @PathVariable String photoType) {
        try {
            List<VehiclePhoto> photos = vehiclePhotoService.getPhotosByType(vehicleId, photoType);
            return ResponseEntity.ok(photos);
        } catch (Exception e) {
            log.error("Error getting photos by type for vehicle {}: {}", vehicleId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/vehicle/{vehicleId}/inspection/{inspectionType}")
    public ResponseEntity<List<VehiclePhoto>> getInspectionPhotos(
            @PathVariable Long vehicleId,
            @PathVariable String inspectionType) {
        try {
            List<VehiclePhoto> photos = vehiclePhotoService.getInspectionPhotos(vehicleId, inspectionType);
            return ResponseEntity.ok(photos);
        } catch (Exception e) {
            log.error("Error getting inspection photos for vehicle {}: {}", vehicleId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/vehicle/{vehicleId}/has-minimum")
    public ResponseEntity<Boolean> hasMinimumPhotos(@PathVariable Long vehicleId) {
        try {
            boolean hasMinimum = vehiclePhotoService.hasMinimumPhotos(vehicleId);
            return ResponseEntity.ok(hasMinimum);
        } catch (Exception e) {
            log.error("Error checking minimum photos for vehicle {}: {}", vehicleId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{photoId}/set-primary")
    @PreAuthorize("hasPermission('VEHICLE_PHOTO_UPLOAD', 'UPDATE')")
    public ResponseEntity<Void> setPrimaryPhoto(
            @PathVariable Long photoId,
            @RequestParam("vehicleId") Long vehicleId) {
        try {
            vehiclePhotoService.setPrimaryPhoto(photoId, vehicleId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error setting primary photo {}: {}", photoId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{photoId}")
    @PreAuthorize("hasPermission('VEHICLE_PHOTO_UPLOAD', 'DELETE')")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long photoId) {
        try {
            vehiclePhotoService.deletePhoto(photoId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting photo {}: {}", photoId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}