package com.example.carrental.config;

import com.example.carrental.model.User;
import com.example.carrental.model.VehicleModel;
import com.example.carrental.model.Role;
import com.example.carrental.model.Permission;
import com.example.carrental.model.Organization;
import com.example.carrental.repository.UserRepository;
import com.example.carrental.repository.VehicleRepository;
import com.example.carrental.repository.RoleRepository;
import com.example.carrental.repository.PermissionRepository;
import com.example.carrental.repository.OrganizationRepository;
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
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        loadInitialData();
    }

    private void loadInitialData() {
        log.info("Loading initial multi-tenant data...");

        // 0. Create organizations first (CRITICAL for multi-tenancy)
        createOrganizations();

        // 1. Create permissions first
        createPermissions();

        // 2. Create roles and assign permissions
        createRoles();

        // 3. Create users and assign to organizations
        createUsers();

        // 4. Create sample vehicles for each organization
        if (vehicleRepository.count() == 0) {
            createSampleVehicles();
        }

        log.info("Multi-tenant data loading completed.");
    }

    private void createOrganizations() {
        log.info("Creating organizations...");

        // Create Demo Company (Default - ID 1)
        if (organizationRepository.findById(1L).isEmpty()) {
            Organization demoCompany = new Organization("Demo Car Rental Company", "demo-company");
            demoCompany.setPlanType(Organization.PlanType.PRO);
            demoCompany.setMaxVehicles(100);
            demoCompany.setMaxEmployees(25);
            demoCompany.setSubscriptionStatus(Organization.SubscriptionStatus.ACTIVE);
            organizationRepository.save(demoCompany);
            log.info("Created Demo Company (ID: 1)");
        }

        // Create Hertz Costa Rica
        if (organizationRepository.findBySlug("hertz-cr").isEmpty()) {
            Organization hertz = new Organization("Hertz Costa Rica", "hertz-cr");
            hertz.setPlanType(Organization.PlanType.ENTERPRISE);
            hertz.setMaxVehicles(500);
            hertz.setMaxEmployees(100);
            hertz.setSubscriptionStatus(Organization.SubscriptionStatus.ACTIVE);
            organizationRepository.save(hertz);
            log.info("Created Hertz Costa Rica");
        }

        // Create Avis Budget Group
        if (organizationRepository.findBySlug("avis-budget").isEmpty()) {
            Organization avis = new Organization("Avis Budget Group", "avis-budget");
            avis.setPlanType(Organization.PlanType.ENTERPRISE);
            avis.setMaxVehicles(300);
            avis.setMaxEmployees(75);
            avis.setSubscriptionStatus(Organization.SubscriptionStatus.ACTIVE);
            organizationRepository.save(avis);
            log.info("Created Avis Budget Group");
        }

        // Create Local Rental (Basic Plan)
        if (organizationRepository.findBySlug("local-rental").isEmpty()) {
            Organization local = new Organization("Rent-A-Car Local", "local-rental");
            local.setPlanType(Organization.PlanType.BASIC);
            local.setMaxVehicles(20);
            local.setMaxEmployees(5);
            local.setSubscriptionStatus(Organization.SubscriptionStatus.ACTIVE);
            organizationRepository.save(local);
            log.info("Created Rent-A-Car Local");
        }

        log.info("Organizations created successfully");
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
            "MAINTENANCE_VIEW", "MAINTENANCE_CREATE", "MAINTENANCE_UPDATE", "MAINTENANCE_RECORD_MANAGE", "MAINTENANCE_RECORD_CREATE",
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
            employeePermissions.add(permissionRepository.findByName("MAINTENANCE_RECORD_CREATE").orElseThrow());
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
        // Get default organization
        Organization defaultOrg = organizationRepository.findById(1L).orElseThrow(
            () -> new RuntimeException("Default organization not found! Create organizations first."));

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
            adminUser.setOrganization(defaultOrg); // CRITICAL: Assign to organization

            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(adminRole);
            adminUser.setRoles(adminRoles);

            userRepository.save(adminUser);
            log.info("Created admin user: admin/admin123 with ADMIN role for organization: {}", defaultOrg.getName());
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
            demoUser.setOrganization(defaultOrg); // CRITICAL: Assign to organization

            Set<Role> customerRoles = new HashSet<>();
            customerRoles.add(customerRole);
            demoUser.setRoles(customerRoles);

            userRepository.save(demoUser);
            log.info("Created demo user: demo/demo123 with CUSTOMER role for organization: {}", defaultOrg.getName());
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
            employeeUser.setOrganization(defaultOrg); // CRITICAL: Assign to organization

            Set<Role> employeeRoles = new HashSet<>();
            employeeRoles.add(employeeRole);
            employeeUser.setRoles(employeeRoles);

            userRepository.save(employeeUser);
            log.info("Created employee user: employee/employee123 with EMPLOYEE role for organization: {}", defaultOrg.getName());
        }

        // Create users for other organizations
        createOrganizationUsers();
    }

    private void createOrganizationUsers() {
        Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();

        // Create user for Hertz Costa Rica (ID: 2)
        Organization hertzOrg = organizationRepository.findBySlug("hertz-cr").orElse(null);
        if (hertzOrg != null && userRepository.findByUsername("demohertz").isEmpty()) {
            User hertzUser = new User();
            hertzUser.setUsername("demohertz");
            hertzUser.setEmail("admin@hertz-cr.com");
            hertzUser.setPassword(passwordEncoder.encode("hertz123"));
            hertzUser.setFirstName("Carlos");
            hertzUser.setLastName("Hertz");
            hertzUser.setIsActive(true);
            hertzUser.setIsLocked(false);
            hertzUser.setFailedLoginAttempts(0);
            hertzUser.setCreatedAt(LocalDateTime.now());
            hertzUser.setUpdatedAt(LocalDateTime.now());
            hertzUser.setOrganization(hertzOrg);

            Set<Role> hertzRoles = new HashSet<>();
            hertzRoles.add(adminRole);
            hertzUser.setRoles(hertzRoles);

            userRepository.save(hertzUser);
            log.info("Created Hertz user: demohertz/hertz123 with ADMIN role for organization: {}", hertzOrg.getName());
        }

        // Create user for Avis Budget Group (ID: 3)
        Organization avisOrg = organizationRepository.findBySlug("avis-budget").orElse(null);
        if (avisOrg != null && userRepository.findByUsername("demoavis").isEmpty()) {
            User avisUser = new User();
            avisUser.setUsername("demoavis");
            avisUser.setEmail("admin@avis-budget.com");
            avisUser.setPassword(passwordEncoder.encode("avis123"));
            avisUser.setFirstName("Maria");
            avisUser.setLastName("Avis");
            avisUser.setIsActive(true);
            avisUser.setIsLocked(false);
            avisUser.setFailedLoginAttempts(0);
            avisUser.setCreatedAt(LocalDateTime.now());
            avisUser.setUpdatedAt(LocalDateTime.now());
            avisUser.setOrganization(avisOrg);

            Set<Role> avisRoles = new HashSet<>();
            avisRoles.add(adminRole);
            avisUser.setRoles(avisRoles);

            userRepository.save(avisUser);
            log.info("Created Avis user: demoavis/avis123 with ADMIN role for organization: {}", avisOrg.getName());
        }

        // Create user for Rent-A-Car Local (ID: 4)
        Organization localOrg = organizationRepository.findBySlug("local-rental").orElse(null);
        if (localOrg != null && userRepository.findByUsername("demolocal").isEmpty()) {
            User localUser = new User();
            localUser.setUsername("demolocal");
            localUser.setEmail("admin@local-rental.com");
            localUser.setPassword(passwordEncoder.encode("local123"));
            localUser.setFirstName("Pedro");
            localUser.setLastName("Local");
            localUser.setIsActive(true);
            localUser.setIsLocked(false);
            localUser.setFailedLoginAttempts(0);
            localUser.setCreatedAt(LocalDateTime.now());
            localUser.setUpdatedAt(LocalDateTime.now());
            localUser.setOrganization(localOrg);

            Set<Role> localRoles = new HashSet<>();
            localRoles.add(adminRole);
            localUser.setRoles(localRoles);

            userRepository.save(localUser);
            log.info("Created Local user: demolocal/local123 with ADMIN role for organization: {}", localOrg.getName());
        }
    }

    private void createSampleVehicles() {
        // Get organizations
        Organization defaultOrg = organizationRepository.findById(1L).orElseThrow();
        Organization hertzOrg = organizationRepository.findBySlug("hertz-cr").orElse(defaultOrg);
        Organization avisOrg = organizationRepository.findBySlug("avis-budget").orElse(defaultOrg);
        Organization localOrg = organizationRepository.findBySlug("local-rental").orElse(defaultOrg);

        // Vehicles for Demo Company (Default)
        VehicleModel[] defaultVehicles = {
            createVehicle("ABC-123", "Toyota", "Corolla", 2020, "Blanco", 45000, 45.00, "COMPACT", 5, "AUTOMATIC", "GASOLINE", defaultOrg),
            createVehicle("DEF-456", "Honda", "Civic", 2019, "Azul", 38000, 42.00, "COMPACT", 5, "MANUAL", "GASOLINE", defaultOrg),
            createVehicle("GHI-789", "Ford", "Focus", 2021, "Rojo", 25000, 48.00, "COMPACT", 5, "AUTOMATIC", "GASOLINE", defaultOrg),
            createVehicle("JKL-012", "Volkswagen", "Golf", 2020, "Negro", 32000, 50.00, "COMPACT", 5, "MANUAL", "GASOLINE", defaultOrg),
            createVehicle("MNO-345", "Nissan", "Sentra", 2018, "Plata", 52000, 40.00, "ECONOMY", 5, "AUTOMATIC", "GASOLINE", defaultOrg)
        };

        // Vehicles for Hertz
        VehicleModel[] hertzVehicles = {
            createVehicle("HTZ-001", "Mercedes", "C-Class", 2022, "Negro", 15000, 85.00, "LUXURY", 5, "AUTOMATIC", "GASOLINE", hertzOrg),
            createVehicle("HTZ-002", "BMW", "320i", 2021, "Blanco", 18000, 80.00, "LUXURY", 5, "AUTOMATIC", "GASOLINE", hertzOrg),
            createVehicle("HTZ-003", "Audi", "A4", 2022, "Azul", 12000, 75.00, "LUXURY", 5, "AUTOMATIC", "GASOLINE", hertzOrg)
        };

        // Vehicles for Avis
        VehicleModel[] avisVehicles = {
            createVehicle("AVS-001", "Toyota", "Camry", 2021, "Gris", 28000, 55.00, "MIDSIZE", 5, "AUTOMATIC", "GASOLINE", avisOrg),
            createVehicle("AVS-002", "Nissan", "Altima", 2020, "Blanco", 35000, 52.00, "MIDSIZE", 5, "AUTOMATIC", "GASOLINE", avisOrg)
        };

        // Vehicles for Local Rental (Basic plan - economy vehicles)
        VehicleModel[] localVehicles = {
            createVehicle("LOC-001", "Hyundai", "Accent", 2019, "Blanco", 42000, 35.00, "ECONOMY", 5, "MANUAL", "GASOLINE", localOrg),
            createVehicle("LOC-002", "Kia", "Rio", 2020, "Azul", 38000, 37.00, "ECONOMY", 5, "MANUAL", "GASOLINE", localOrg),
            createVehicle("LOC-003", "Chevrolet", "Spark", 2018, "Rojo", 48000, 32.00, "ECONOMY", 4, "MANUAL", "GASOLINE", localOrg)
        };

        // Save all vehicles
        for (VehicleModel vehicle : defaultVehicles) {
            vehicleRepository.save(vehicle);
        }
        for (VehicleModel vehicle : hertzVehicles) {
            vehicleRepository.save(vehicle);
        }
        for (VehicleModel vehicle : avisVehicles) {
            vehicleRepository.save(vehicle);
        }
        for (VehicleModel vehicle : localVehicles) {
            vehicleRepository.save(vehicle);
        }

        log.info("Created {} sample vehicles distributed across organizations:",
                defaultVehicles.length + hertzVehicles.length + avisVehicles.length + localVehicles.length);
        log.info("- {} vehicles for {}", defaultVehicles.length, defaultOrg.getName());
        log.info("- {} vehicles for {}", hertzVehicles.length, hertzOrg.getName());
        log.info("- {} vehicles for {}", avisVehicles.length, avisOrg.getName());
        log.info("- {} vehicles for {}", localVehicles.length, localOrg.getName());
    }

    private VehicleModel createVehicle(String licensePlate, String brand, String model, int year,
                                String color, int mileage, double dailyRate, String category,
                                int seats, String transmission, String fuelType, Organization organization) {
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
        vehicle.setOrganization(organization); // CRITICAL: Assign to organization

        return vehicle;
    }
}