package com.example.carrental.repository;

import com.example.carrental.model.VehiclePhoto;
import com.example.carrental.model.VehicleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehiclePhotoRepository extends JpaRepository<VehiclePhoto, Long> {

    List<VehiclePhoto> findByVehicleOrderByCreatedAtDesc(VehicleModel vehicle);

    List<VehiclePhoto> findByVehicleIdOrderByCreatedAtDesc(Long vehicleId);

    List<VehiclePhoto> findByVehicleAndPhotoTypeOrderByCreatedAtDesc(VehicleModel vehicle, String photoType);

    List<VehiclePhoto> findByVehicleAndInspectionTypeOrderByCreatedAtDesc(VehicleModel vehicle, String inspectionType);

    Optional<VehiclePhoto> findByVehicleAndIsPrimaryTrue(VehicleModel vehicle);

    @Query("SELECT vp FROM VehiclePhoto vp WHERE vp.vehicle = :vehicle AND vp.photoType = 'GENERAL' ORDER BY vp.createdAt DESC")
    List<VehiclePhoto> findGeneralPhotosByVehicle(@Param("vehicle") VehicleModel vehicle);

    @Query("SELECT COUNT(vp) FROM VehiclePhoto vp WHERE vp.vehicle = :vehicle")
    long countPhotosByVehicle(@Param("vehicle") VehicleModel vehicle);

    @Query("SELECT COUNT(vp) FROM VehiclePhoto vp WHERE vp.vehicle = :vehicle AND vp.photoType = 'GENERAL'")
    long countGeneralPhotosByVehicle(@Param("vehicle") VehicleModel vehicle);

    void deleteByVehicle(VehicleModel vehicle);
}