package com.example.carrental.repository;

import com.example.carrental.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.name = :permissionName")
    List<Role> findByPermissionName(@Param("permissionName") String permissionName);

    @Query("SELECT r FROM Role r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Role> searchRoles(@Param("search") String search);
}