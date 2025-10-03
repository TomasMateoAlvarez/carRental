package com.example.carrental.config;

import com.example.carrental.model.User;
import com.example.carrental.model.VehicleModel;
import com.example.carrental.repository.UserRepository;
import com.example.carrental.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        loadInitialData();
    }

    private void loadInitialData() {
        log.info("Loading initial data...");

        // Create admin user if it doesn't exist
        if (userRepository.findByUsername("admin").isEmpty()) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@carrental.com");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setFirstName("Admin");
            adminUser.setLastName("User");
            adminUser.setIsActive(true);
            adminUser.setIsLocked(false);
            adminUser.setFailedLoginAttempts(0);
            adminUser.setCreatedAt(LocalDateTime.now());
            adminUser.setUpdatedAt(LocalDateTime.now());

            userRepository.save(adminUser);
            log.info("Created admin user: admin/admin123");
        }

        // Create demo user if it doesn't exist
        if (userRepository.findByUsername("demo").isEmpty()) {
            User demoUser = new User();
            demoUser.setUsername("demo");
            demoUser.setEmail("demo@carrental.com");
            demoUser.setPassword(passwordEncoder.encode("demo123"));
            demoUser.setFirstName("Demo");
            demoUser.setLastName("User");
            demoUser.setIsActive(true);
            demoUser.setIsLocked(false);
            demoUser.setFailedLoginAttempts(0);
            demoUser.setCreatedAt(LocalDateTime.now());
            demoUser.setUpdatedAt(LocalDateTime.now());

            userRepository.save(demoUser);
            log.info("Created demo user: demo/demo123");
        }

        // Create sample vehicles if none exist
        if (vehicleRepository.count() == 0) {
            createSampleVehicles();
        }

        log.info("Initial data loading completed.");
    }

    private void createSampleVehicles() {
        VehicleModel[] vehicles = {
            createVehicle("ABC-123", "Toyota", "Corolla", 2020, "Blanco", 45000, 45.00, "COMPACT", 5, "AUTOMATIC", "GASOLINE"),
            createVehicle("DEF-456", "Honda", "Civic", 2019, "Azul", 38000, 42.00, "COMPACT", 5, "MANUAL", "GASOLINE"),
            createVehicle("GHI-789", "Ford", "Focus", 2021, "Rojo", 25000, 48.00, "COMPACT", 5, "AUTOMATIC", "GASOLINE"),
            createVehicle("JKL-012", "Volkswagen", "Golf", 2020, "Negro", 32000, 50.00, "COMPACT", 5, "MANUAL", "GASOLINE"),
            createVehicle("MNO-345", "Nissan", "Sentra", 2018, "Plata", 52000, 40.00, "ECONOMY", 5, "AUTOMATIC", "GASOLINE"),
            createVehicle("PQR-678", "Chevrolet", "Cruze", 2019, "Gris", 41000, 44.00, "COMPACT", 5, "AUTOMATIC", "GASOLINE"),
            createVehicle("STU-901", "Hyundai", "Elantra", 2021, "Blanco", 28000, 46.00, "COMPACT", 5, "AUTOMATIC", "GASOLINE"),
            createVehicle("VWX-234", "Mazda", "3", 2020, "Azul", 35000, 47.00, "COMPACT", 5, "MANUAL", "GASOLINE"),
            createVehicle("YZA-567", "Kia", "Forte", 2019, "Negro", 43000, 41.00, "ECONOMY", 5, "AUTOMATIC", "GASOLINE"),
            createVehicle("BCD-890", "Subaru", "Impreza", 2021, "Verde", 22000, 49.00, "COMPACT", 5, "MANUAL", "GASOLINE")
        };

        for (VehicleModel vehicle : vehicles) {
            vehicleRepository.save(vehicle);
        }

        log.info("Created {} sample vehicles", vehicles.length);
    }

    private VehicleModel createVehicle(String licensePlate, String brand, String model, int year,
                                String color, int mileage, double dailyRate, String category,
                                int seats, String transmission, String fuelType) {
        VehicleModel vehicle = new VehicleModel();
        vehicle.setLicensePlate(licensePlate);
        vehicle.setBrand(brand);
        vehicle.setModel(model);
        vehicle.setYear(year);
        vehicle.setColor(color);
        vehicle.setMileage(mileage);
        vehicle.setDailyRate(BigDecimal.valueOf(dailyRate));
        vehicle.setCategory(category);
        vehicle.setSeats(seats);
        vehicle.setTransmission(transmission);
        vehicle.setFuelType(fuelType);
        vehicle.setStatus(com.example.carrental.enums.VehicleStatus.AVAILABLE);
        vehicle.setCreatedAt(LocalDateTime.now());
        vehicle.setUpdatedAt(LocalDateTime.now());
        vehicle.setDescription("Vehículo en excelente estado, ideal para uso urbano y viajes.");

        return vehicle;
    }
}