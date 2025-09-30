package com.example.carrental.repository;

import com.example.carrental.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByIsActive(Boolean isActive);

    List<User> findByIsLocked(Boolean isLocked);

    @Query("SELECT u FROM User u WHERE u.lastLogin < :date")
    List<User> findUsersWithLastLoginBefore(@Param("date") LocalDateTime date);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);

    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<User> searchUsers(@Param("search") String search);

    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    long countActiveUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :date")
    long countUsersCreatedAfter(@Param("date") LocalDateTime date);
}