package com.example.carrental.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_code", unique = true, nullable = false, length = 50)
    private String tenantCode; // Unique identifier for the company

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @Column(name = "domain", unique = true, length = 100)
    private String domain; // Custom domain for the tenant

    @Column(name = "subdomain", unique = true, length = 50)
    private String subdomain; // tenant.carrental.com

    // Contact Information
    @Column(name = "contact_email", length = 150)
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    // Address Information
    @Column(name = "address_line1", length = 200)
    private String addressLine1;

    @Column(name = "address_line2", length = 200)
    private String addressLine2;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "country", length = 100)
    private String country;

    // Business Information
    @Column(name = "tax_id", length = 50)
    private String taxId;

    @Column(name = "business_license", length = 100)
    private String businessLicense;

    @Column(name = "industry", length = 100)
    private String industry;

    @Column(name = "company_size", length = 50)
    private String companySize; // SMALL, MEDIUM, LARGE, ENTERPRISE

    // Subscription & Billing
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan", nullable = false)
    @Builder.Default
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.STARTER;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", nullable = false)
    @Builder.Default
    private BillingCycle billingCycle = BillingCycle.MONTHLY;

    @Column(name = "max_users")
    private Integer maxUsers;

    @Column(name = "max_vehicles")
    private Integer maxVehicles;

    @Column(name = "max_reservations_per_month")
    private Integer maxReservationsPerMonth;

    // Feature Flags
    @Column(name = "custom_branding_enabled")
    @Builder.Default
    private Boolean customBrandingEnabled = false;

    @Column(name = "advanced_analytics_enabled")
    @Builder.Default
    private Boolean advancedAnalyticsEnabled = false;

    @Column(name = "api_access_enabled")
    @Builder.Default
    private Boolean apiAccessEnabled = false;

    @Column(name = "white_label_enabled")
    @Builder.Default
    private Boolean whiteLabelEnabled = false;

    @Column(name = "custom_domain_enabled")
    @Builder.Default
    private Boolean customDomainEnabled = false;

    // Customization
    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "primary_color", length = 7)
    private String primaryColor; // Hex color code

    @Column(name = "secondary_color", length = 7)
    private String secondaryColor;

    @Column(name = "custom_css", columnDefinition = "TEXT")
    private String customCss;

    @Column(name = "timezone", length = 50)
    @Builder.Default
    private String timezone = "UTC";

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "language", length = 5)
    @Builder.Default
    private String language = "en-US";

    // Status & Lifecycle
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private TenantStatus status = TenantStatus.ACTIVE;

    @Column(name = "trial_ends_at")
    private LocalDateTime trialEndsAt;

    @Column(name = "subscription_ends_at")
    private LocalDateTime subscriptionEndsAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // Stripe Integration
    @Column(name = "stripe_customer_id", length = 100)
    private String stripeCustomerId;

    @Column(name = "stripe_subscription_id", length = 100)
    private String stripeSubscriptionId;

    // Metadata
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON for additional custom fields

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "onboarded_at")
    private LocalDateTime onboardedAt;

    // Relationships
    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> users;

    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VehicleModel> vehicles;

    // Business Logic Methods
    public boolean isTrialActive() {
        return trialEndsAt != null && trialEndsAt.isAfter(LocalDateTime.now());
    }

    public boolean isSubscriptionActive() {
        return subscriptionEndsAt != null && subscriptionEndsAt.isAfter(LocalDateTime.now());
    }

    public boolean canAccessFeature(String featureName) {
        return switch (featureName.toLowerCase()) {
            case "custom_branding" -> customBrandingEnabled &&
                (subscriptionPlan == SubscriptionPlan.BUSINESS || subscriptionPlan == SubscriptionPlan.ENTERPRISE);
            case "advanced_analytics" -> advancedAnalyticsEnabled &&
                (subscriptionPlan == SubscriptionPlan.BUSINESS || subscriptionPlan == SubscriptionPlan.ENTERPRISE);
            case "api_access" -> apiAccessEnabled && subscriptionPlan == SubscriptionPlan.ENTERPRISE;
            case "white_label" -> whiteLabelEnabled && subscriptionPlan == SubscriptionPlan.ENTERPRISE;
            case "custom_domain" -> customDomainEnabled && subscriptionPlan == SubscriptionPlan.ENTERPRISE;
            default -> false;
        };
    }

    public boolean hasReachedUserLimit() {
        return maxUsers != null && users != null && users.size() >= maxUsers;
    }

    public boolean hasReachedVehicleLimit() {
        return maxVehicles != null && vehicles != null && vehicles.size() >= maxVehicles;
    }

    public String getFullDomain() {
        if (customDomainEnabled && domain != null) {
            return domain;
        }
        return subdomain + ".carrental.com";
    }

    // Enums
    public enum SubscriptionPlan {
        STARTER("Starter", 5, 10, 50),
        BUSINESS("Business", 25, 50, 500),
        ENTERPRISE("Enterprise", -1, -1, -1); // Unlimited

        private final String displayName;
        private final int maxUsers;
        private final int maxVehicles;
        private final int maxReservationsPerMonth;

        SubscriptionPlan(String displayName, int maxUsers, int maxVehicles, int maxReservationsPerMonth) {
            this.displayName = displayName;
            this.maxUsers = maxUsers;
            this.maxVehicles = maxVehicles;
            this.maxReservationsPerMonth = maxReservationsPerMonth;
        }

        public String getDisplayName() { return displayName; }
        public int getMaxUsers() { return maxUsers; }
        public int getMaxVehicles() { return maxVehicles; }
        public int getMaxReservationsPerMonth() { return maxReservationsPerMonth; }
    }

    public enum BillingCycle {
        MONTHLY("Monthly"),
        QUARTERLY("Quarterly"),
        ANNUALLY("Annually");

        private final String displayName;

        BillingCycle(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    public enum TenantStatus {
        ACTIVE("Active"),
        TRIAL("Trial"),
        SUSPENDED("Suspended"),
        CANCELLED("Cancelled"),
        PENDING_SETUP("Pending Setup");

        private final String displayName;

        TenantStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }
}