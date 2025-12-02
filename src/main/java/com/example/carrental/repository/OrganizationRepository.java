package com.example.carrental.repository;

import com.example.carrental.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    Optional<Organization> findBySlug(String slug);

    Optional<Organization> findByName(String name);

    @Query("SELECT COUNT(o) FROM Organization o WHERE o.subscriptionStatus = 'ACTIVE'")
    long countActiveOrganizations();

    @Query("SELECT o FROM Organization o WHERE o.subscriptionStatus = 'ACTIVE'")
    java.util.List<Organization> findAllActive();

    boolean existsBySlug(String slug);

    boolean existsByName(String name);
}