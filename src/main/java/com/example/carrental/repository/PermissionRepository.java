package com.example.carrental.repository;

import com.example.carrental.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(String name);

    boolean existsByName(String name);

    List<Permission> findByResource(String resource);

    List<Permission> findByAction(String action);

    List<Permission> findByResourceAndAction(String resource, String action);

    @Query("SELECT p FROM Permission p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.resource) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.action) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Permission> searchPermissions(@Param("search") String search);
}