package com.example.carrental.controller;

import com.example.carrental.model.Tenant;
import com.example.carrental.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class TenantController {

    private final TenantRepository tenantRepository;

    @GetMapping
    public ResponseEntity<List<Tenant>> getAllTenants() {
        List<Tenant> tenants = tenantRepository.findAll();
        return ResponseEntity.ok(tenants);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Tenant>> getActiveTenants() {
        List<Tenant> tenants = tenantRepository.findByIsActiveTrue();
        return ResponseEntity.ok(tenants);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tenant> getTenant(@PathVariable Long id) {
        Optional<Tenant> tenant = tenantRepository.findById(id);
        return tenant.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{tenantCode}")
    public ResponseEntity<Tenant> getTenantByCode(@PathVariable String tenantCode) {
        Optional<Tenant> tenant = tenantRepository.findByTenantCode(tenantCode);
        return tenant.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/domain/{domain}")
    public ResponseEntity<Tenant> getTenantByDomain(@PathVariable String domain) {
        Optional<Tenant> tenant = tenantRepository.findByDomain(domain);
        return tenant.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Tenant> createTenant(@Valid @RequestBody Tenant tenant) {
        // Generate tenant code if not provided
        if (tenant.getTenantCode() == null) {
            tenant.setTenantCode(generateTenantCode(tenant.getCompanyName()));
        }

        // Set default values
        tenant.setCreatedAt(LocalDateTime.now());
        tenant.setUpdatedAt(LocalDateTime.now());
        tenant.setIsActive(true);
        tenant.setStatus(Tenant.TenantStatus.PENDING_SETUP);

        // Set subscription limits based on plan
        setSubscriptionLimits(tenant);

        Tenant savedTenant = tenantRepository.save(tenant);
        return ResponseEntity.ok(savedTenant);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tenant> updateTenant(@PathVariable Long id, @Valid @RequestBody Tenant tenantDetails) {
        Optional<Tenant> optionalTenant = tenantRepository.findById(id);

        if (optionalTenant.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Tenant tenant = optionalTenant.get();

        // Update fields
        tenant.setCompanyName(tenantDetails.getCompanyName());
        tenant.setContactEmail(tenantDetails.getContactEmail());
        tenant.setContactPhone(tenantDetails.getContactPhone());
        tenant.setContactPerson(tenantDetails.getContactPerson());
        tenant.setAddressLine1(tenantDetails.getAddressLine1());
        tenant.setAddressLine2(tenantDetails.getAddressLine2());
        tenant.setCity(tenantDetails.getCity());
        tenant.setState(tenantDetails.getState());
        tenant.setPostalCode(tenantDetails.getPostalCode());
        tenant.setCountry(tenantDetails.getCountry());
        tenant.setSubscriptionPlan(tenantDetails.getSubscriptionPlan());
        tenant.setBillingCycle(tenantDetails.getBillingCycle());
        tenant.setUpdatedAt(LocalDateTime.now());

        // Update subscription limits based on new plan
        setSubscriptionLimits(tenant);

        Tenant savedTenant = tenantRepository.save(tenant);
        return ResponseEntity.ok(savedTenant);
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<Tenant> activateTenant(@PathVariable Long id) {
        Optional<Tenant> optionalTenant = tenantRepository.findById(id);

        if (optionalTenant.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Tenant tenant = optionalTenant.get();
        tenant.setIsActive(true);
        tenant.setStatus(Tenant.TenantStatus.ACTIVE);
        tenant.setOnboardedAt(LocalDateTime.now());
        tenant.setUpdatedAt(LocalDateTime.now());

        Tenant savedTenant = tenantRepository.save(tenant);
        return ResponseEntity.ok(savedTenant);
    }

    @PutMapping("/{id}/suspend")
    public ResponseEntity<Tenant> suspendTenant(@PathVariable Long id) {
        Optional<Tenant> optionalTenant = tenantRepository.findById(id);

        if (optionalTenant.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Tenant tenant = optionalTenant.get();
        tenant.setStatus(Tenant.TenantStatus.SUSPENDED);
        tenant.setUpdatedAt(LocalDateTime.now());

        Tenant savedTenant = tenantRepository.save(tenant);
        return ResponseEntity.ok(savedTenant);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTenant(@PathVariable Long id) {
        if (!tenantRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        tenantRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/subscription/{subscriptionPlan}")
    public ResponseEntity<List<Tenant>> getTenantsBySubscriptionPlan(
            @PathVariable Tenant.SubscriptionPlan subscriptionPlan) {
        List<Tenant> tenants = tenantRepository.findBySubscriptionPlanAndIsActiveTrue(subscriptionPlan, true);
        return ResponseEntity.ok(tenants);
    }

    @GetMapping("/expired-trials")
    public ResponseEntity<List<Tenant>> getExpiredTrialTenants() {
        List<Tenant> tenants = tenantRepository.findExpiredTrialTenants();
        return ResponseEntity.ok(tenants);
    }

    @GetMapping("/expired-subscriptions")
    public ResponseEntity<List<Tenant>> getExpiredSubscriptionTenants() {
        List<Tenant> tenants = tenantRepository.findExpiredSubscriptionTenants();
        return ResponseEntity.ok(tenants);
    }

    private String generateTenantCode(String companyName) {
        String cleanName = companyName.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        String prefix = cleanName.length() >= 3 ? cleanName.substring(0, 3) : cleanName;
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
        return prefix + timestamp;
    }

    private void setSubscriptionLimits(Tenant tenant) {
        Tenant.SubscriptionPlan plan = tenant.getSubscriptionPlan();
        tenant.setMaxUsers(plan.getMaxUsers());
        tenant.setMaxVehicles(plan.getMaxVehicles());
        tenant.setMaxReservationsPerMonth(plan.getMaxReservationsPerMonth());

        // Set feature flags based on subscription plan
        switch (plan) {
            case STARTER:
                tenant.setCustomBrandingEnabled(false);
                tenant.setAdvancedAnalyticsEnabled(false);
                tenant.setApiAccessEnabled(false);
                tenant.setWhiteLabelEnabled(false);
                tenant.setCustomDomainEnabled(false);
                break;
            case BUSINESS:
                tenant.setCustomBrandingEnabled(true);
                tenant.setAdvancedAnalyticsEnabled(true);
                tenant.setApiAccessEnabled(false);
                tenant.setWhiteLabelEnabled(false);
                tenant.setCustomDomainEnabled(false);
                break;
            case ENTERPRISE:
                tenant.setCustomBrandingEnabled(true);
                tenant.setAdvancedAnalyticsEnabled(true);
                tenant.setApiAccessEnabled(true);
                tenant.setWhiteLabelEnabled(true);
                tenant.setCustomDomainEnabled(true);
                break;
        }
    }
}