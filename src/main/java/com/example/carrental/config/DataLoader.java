package com.example.carrental.config;

import com.example.carrental.model.User;
import com.example.carrental.model.VehicleModel;
import com.example.carrental.model.Role;
import com.example.carrental.model.Permission;
import com.example.carrental.repository.UserRepository;
import com.example.carrental.repository.VehicleRepository;
import com.example.carrental.repository.RoleRepository;
import com.example.carrental.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        loadInitialData();
    }

    private void loadInitialData() {
        log.info("Loading initial data...");

        // 1. Create permissions first
        createPermissions();

        // 2. Create roles and assign permissions
        createRoles();

        // 3. Create users and assign roles
        createUsers();

        // 4. Create sample vehicles if none exist
        if (vehicleRepository.count() == 0) {
            createSampleVehicles();
        }

        log.info("Initial data loading completed.");
    }

    private void createPermissions() {
        String[] permissionNames = {
            // Vehicle permissions
            "VEHICLE_VIEW", "VEHICLE_CREATE", "VEHICLE_UPDATE", "VEHICLE_DELETE", "VEHICLE_STATUS_CHANGE",
            // Reservation permissions
            "RESERVATION_VIEW", "RESERVATION_CREATE", "RESERVATION_UPDATE", "RESERVATION_DELETE", "RESERVATION_MANAGE",
            // User permissions
            "USER_VIEW", "USER_CREATE", "USER_UPDATE", "USER_DELETE",
            // Dashboard permissions
            "DASHBOARD_VIEW", "ANALYTICS_VIEW",
            // Maintenance permissions
            "MAINTENANCE_VIEW", "MAINTENANCE_CREATE", "MAINTENANCE_UPDATE", "MAINTENANCE_RECORD_MANAGE",
            // Vehicle photo permissions
            "VEHICLE_PHOTO_UPLOAD", "VEHICLE_PHOTO_VIEW", "VEHICLE_PHOTO_DELETE",
            // Notification permissions
            "NOTIFICATION_MANAGE", "NOTIFICATION_VIEW", "NOTIFICATION_CREATE"
        };

        for (String permName : permissionNames) {
            if (permissionRepository.findByName(permName).isEmpty()) {
                Permission permission = Permission.builder()
                    .name(permName)
                    .resource(permName.split("_")[0])
                    .action(permName.split("_")[1])
                    .description("Permission to " + permName.toLowerCase().replace("_", " "))
                    .createdAt(LocalDateTime.now())
                    .build();
                permissionRepository.save(permission);
            }
        }
        log.info("Created permissions");
    }

    private void createRoles() {
        // ADMIN Role - Full access
        Role adminRole = createRoleIfNotExists("ADMIN", "Full system administrator");
        if (adminRole.getPermissions().isEmpty()) {
            Set<Permission> adminPermissions = new HashSet<>(permissionRepository.findAll());
            adminRole.setPermissions(adminPermissions);
            roleRepository.save(adminRole);
        }

        // EMPLOYEE Role - Limited to vehicle status changes only
        Role employeeRole = createRoleIfNotExists("EMPLOYEE", "Employee with limited vehicle management");
        if (employeeRole.getPermissions().isEmpty()) {
            Set<Permission> employeePermissions = new HashSet<>();
            employeePermissions.add(permissionRepository.findByName("VEHICLE_VIEW").orElseThrow());
            employeePermissions.add(permissionRepository.findByName("VEHICLE_STATUS_CHANGE").orElseThrow());
            employeePermissions.add(permissionRepository.findByName("RESERVATION_VIEW").orElseThrow());
            employeePermissions.add(permissionRepository.findByName("RESERVATION_MANAGE").orElseThrow());
            employeePermissions.add(permissionRepository.findByName("DASHBOARD_VIEW").orElseThrow());
            employeePermissions.add(permissionRepository.findByName("MAINTENANCE_VIEW").orElseThrow());
            employeePermissions.add(permissionRepository.findByName("MAINTENANCE_RECORD_MANAGE").orElseThrow());
            employeePermissions.add(permissionRepository.findByName("VEHICLE_PHOTO_UPLOAD").orElseThrow());
            employeePermissions.add(permissionRepository.findByName("VEHICLE_PHOTO_VIEW").orElseThrow());
            employeePermissions.add(permissionRepository.findByName("NOTIFICATION_VIEW").orElseThrow());
            employeeRole.setPermissions(employeePermissions);
            roleRepository.save(employeeRole);
        }

        // CUSTOMER Role - Basic customer access
        Role customerRole = createRoleIfNotExists("CUSTOMER", "Regular customer");
        if (customerRole.getPermissions().isEmpty()) {
            Set<Permission> customerPermissions = new HashSet<>();
            customerPermissions.add(permissionRepository.findByName("VEHICLE_VIEW").orElseThrow());
            customerPermissions.add(permissionRepository.findByName("RESERVATION_VIEW").orElseThrow());
            customerPermissions.add(permissionRepository.findByName("RESERVATION_CREATE").orElseThrow());
            customerRole.setPermissions(customerPermissions);
            roleRepository.save(customerRole);
        }

        log.info("Created roles with permissions");
    }

    private Role createRoleIfNotExists(String name, String description) {
        return roleRepository.findByName(name).orElseGet(() -> {
            Role role = Role.builder()
                .name(name)
                .description(description)
                .createdAt(LocalDateTime.now())
                .permissions(new HashSet<>())
                .build();
            return roleRepository.save(role);
        });
    }

    private void createUsers() {
        // Create admin user if it doesn't exist
        if (userRepository.findByUsername("admin").isEmpty()) {
            Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();

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

            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(adminRole);
            adminUser.setRoles(adminRoles);

            userRepository.save(adminUser);
            log.info("Created admin user: admin/admin123 with ADMIN role");
        }

        // Create demo customer user if it doesn't exist
        if (userRepository.findByUsername("demo").isEmpty()) {
            Role customerRole = roleRepository.findByName("CUSTOMER").orElseThrow();

            User demoUser = new User();
            demoUser.setUsername("demo");
            demoUser.setEmail("demo@carrental.com");
            demoUser.setPassword(passwordEncoder.encode("demo123"));
            demoUser.setFirstName("Demo");
            demoUser.setLastName("Customer");
            demoUser.setIsActive(true);
            demoUser.setIsLocked(false);
            demoUser.setFailedLoginAttempts(0);
            demoUser.setCreatedAt(LocalDateTime.now());
            demoUser.setUpdatedAt(LocalDateTime.now());

            Set<Role> customerRoles = new HashSet<>();
            customerRoles.add(customerRole);
            demoUser.setRoles(customerRoles);

            userRepository.save(demoUser);
            log.info("Created demo user: demo/demo123 with CUSTOMER role");
        }

        // Create employee user if it doesn't exist
        if (userRepository.findByUsername("employee").isEmpty()) {
            Role employeeRole = roleRepository.findByName("EMPLOYEE").orElseThrow();

            User employeeUser = new User();
            employeeUser.setUsername("employee");
            employeeUser.setEmail("employee@carrental.com");
            employeeUser.setPassword(passwordEncoder.encode("employee123"));
            employeeUser.setFirstName("Juan");
            employeeUser.setLastName("Empleado");
            employeeUser.setIsActive(true);
            employeeUser.setIsLocked(false);
            employeeUser.setFailedLoginAttempts(0);
            employeeUser.setCreatedAt(LocalDateTime.now());
            employeeUser.setUpdatedAt(LocalDateTime.now());

            Set<Role> employeeRoles = new HashSet<>();
            employeeRoles.add(employeeRole);
            employeeUser.setRoles(employeeRoles);

            userRepository.save(employeeUser);
            log.info("Created employee user: employee/employee123 with EMPLOYEE role");
        }
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