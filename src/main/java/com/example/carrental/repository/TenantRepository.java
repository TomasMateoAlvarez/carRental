package com.example.carrental.repository;

import com.example.carrental.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findByTenantCode(String tenantCode);

    Optional<Tenant> findByDomain(String domain);

    Optional<Tenant> findBySubdomain(String subdomain);

    List<Tenant> findByIsActiveTrue();

    @Query("SELECT t FROM Tenant t WHERE t.subscriptionPlan = :subscriptionPlan AND t.isActive = :isActive")
    List<Tenant> findBySubscriptionPlanAndIsActiveTrue(@Param("subscriptionPlan") Tenant.SubscriptionPlan subscriptionPlan,
                                                       @Param("isActive") Boolean isActive);

    List<Tenant> findByStatus(Tenant.TenantStatus status);

    @Query("SELECT t FROM Tenant t WHERE t.trialEndsAt < CURRENT_TIMESTAMP AND t.status = 'TRIAL'")
    List<Tenant> findExpiredTrialTenants();

    @Query("SELECT t FROM Tenant t WHERE t.subscriptionEndsAt < CURRENT_TIMESTAMP AND t.status = 'ACTIVE'")
    List<Tenant> findExpiredSubscriptionTenants();

    List<Tenant> findByCompanyNameContainingIgnoreCase(String companyName);

    Optional<Tenant> findByStripeCustomerId(String stripeCustomerId);
}