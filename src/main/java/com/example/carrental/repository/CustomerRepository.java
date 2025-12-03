package com.example.carrental.repository;

import com.example.carrental.enums.CustomerSegment;
import com.example.carrental.enums.CustomerStatus;
import com.example.carrental.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Multi-tenant basic queries
    @Query("SELECT c FROM Customer c WHERE c.organization.id = :#{@tenantContext.getTenantId()}")
    List<Customer> findAll();

    @Query("SELECT c FROM Customer c WHERE c.organization.id = :#{@tenantContext.getTenantId()} AND c.id = :id")
    Optional<Customer> findById(Long id);

    @Query("SELECT c FROM Customer c WHERE c.organization.id = :#{@tenantContext.getTenantId()} AND c.customerCode = :customerCode")
    Optional<Customer> findByCustomerCode(@Param("customerCode") String customerCode);

    @Query("SELECT c FROM Customer c WHERE c.organization.id = :#{@tenantContext.getTenantId()} AND c.email = :email")
    Optional<Customer> findByEmail(@Param("email") String email);

    @Query("SELECT c FROM Customer c WHERE c.organization.id = :#{@tenantContext.getTenantId()} AND c.licenseNumber = :licenseNumber")
    Optional<Customer> findByLicenseNumber(@Param("licenseNumber") String licenseNumber);

    // Status and segment queries
    @Query("SELECT c FROM Customer c WHERE c.organization.id = :#{@tenantContext.getTenantId()} AND c.status = :status")
    List<Customer> findByStatus(@Param("status") CustomerStatus status);

    @Query("SELECT c FROM Customer c WHERE c.organization.id = :#{@tenantContext.getTenantId()} AND c.segment = :segment")
    List<Customer> findBySegment(@Param("segment") CustomerSegment segment);

    @Query("SELECT c FROM Customer c WHERE c.organization.id = :#{@tenantContext.getTenantId()} AND c.status = :status")
    Page<Customer> findByStatus(@Param("status") CustomerStatus status, Pageable pageable);

    // Business analytics queries
    @Query("SELECT c FROM Customer c WHERE c.organization.id = :#{@tenantContext.getTenantId()} AND c.totalReservations >= :minReservations")
    List<Customer> findByTotalReservationsGreaterThanEqual(@Param("minReservations") Integer minReservations);

    @Query("SELECT c FROM Customer c WHERE c.organization.id = :#{@tenantContext.getTenantId()} AND c.totalSpent >= :minSpent")
    List<Customer> findHighValueCustomers(@Param("minSpent") BigDecimal minSpent);

    @Query("SELECT c FROM Customer c WHERE c.organization.id = :#{@tenantContext.getTenantId()} AND c.lastRentalDate >= :fromDate")
    List<Customer> findRecentlyActiveCustomers(@Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT c FROM Customer c WHERE c.organization.id = :#{@tenantContext.getTenantId()} AND " +
           "(c.lastRentalDate IS NULL OR c.lastRentalDate < :beforeDate)")
    List<Customer> findInactiveCustomers(@Param("beforeDate") LocalDateTime beforeDate);

    // License expiry queries
    @Query("SELECT c FROM Customer c WHERE c.organization.id = :#{@tenantContext.getTenantId()} AND " +
           "c.licenseExpiryDate BETWEEN :fromDate AND :toDate")
    List<Customer> findCustomersWithExpiringLicense(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query("SELECT c FROM Customer c WHERE c.organization.id = :#{@tenantContext.getTenantId()} AND " +
           "c.licenseExpiryDate <= :date")
    List<Customer> findCustomersWithExpiredLicense(@Param("date") LocalDate date);

    // Search functionality
    @Query("SELECT c FROM Customer c WHERE c.organization.id = :#{@tenantContext.getTenantId()} AND (" +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.customerCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.licenseNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Customer> searchCustomers(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Customer statistics queries
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.organization.id = :#{@tenantContext.getTenantId()} AND c.status = :status")
    long countByStatus(@Param("status") CustomerStatus status);

    @Query("SELECT c.segment, COUNT(c) FROM Customer c WHERE c.organization.id = :#{@tenantContext.getTenantId()} GROUP BY c.segment")
    List<Object[]> countCustomersBySegment();

    @Query("SELECT c.preferredVehicleCategory, COUNT(c) FROM Customer c " +
           "WHERE c.organization.id = :#{@tenantContext.getTenantId()} AND c.preferredVehicleCategory IS NOT NULL " +
           "GROUP BY c.preferredVehicleCategory")
    List<Object[]> countCustomersByPreferredCategory();

    // Customer with reservation history
    @Query("SELECT DISTINCT c FROM Customer c LEFT JOIN FETCH c.reservations r " +
           "WHERE c.organization.id = :#{@tenantContext.getTenantId()} AND c.id = :customerId")
    Optional<Customer> findByIdWithReservations(@Param("customerId") Long customerId);

    @Query("SELECT c FROM Customer c WHERE c.organization.id = :#{@tenantContext.getTenantId()} AND " +
           "c.id IN (SELECT DISTINCT r.customer.id FROM Reservation r WHERE r.customer.id IS NOT NULL)")
    List<Customer> findCustomersWithReservations();

    // Birthday and anniversary queries
    @Query("SELECT c FROM Customer c WHERE c.organization.id = :#{@tenantContext.getTenantId()} AND " +
           "FUNCTION('DAY', c.dateOfBirth) = FUNCTION('DAY', :date) AND " +
           "FUNCTION('MONTH', c.dateOfBirth) = FUNCTION('MONTH', :date)")
    List<Customer> findCustomersWithBirthdayOn(@Param("date") LocalDate date);

    @Query("SELECT c FROM Customer c WHERE c.organization.id = :#{@tenantContext.getTenantId()} AND " +
           "FUNCTION('DAY', c.createdAt) = FUNCTION('DAY', :date) AND " +
           "FUNCTION('MONTH', c.createdAt) = FUNCTION('MONTH', :date)")
    List<Customer> findCustomersWithAnniversaryOn(@Param("date") LocalDate date);

    // Validation queries
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Customer c " +
           "WHERE c.organization.id = :#{@tenantContext.getTenantId()} AND c.customerCode = :customerCode")
    boolean existsByCustomerCode(@Param("customerCode") String customerCode);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Customer c " +
           "WHERE c.organization.id = :#{@tenantContext.getTenantId()} AND c.email = :email")
    boolean existsByEmail(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Customer c " +
           "WHERE c.organization.id = :#{@tenantContext.getTenantId()} AND c.licenseNumber = :licenseNumber")
    boolean existsByLicenseNumber(@Param("licenseNumber") String licenseNumber);

    // Top customers queries
    @Query("SELECT c FROM Customer c WHERE c.organization.id = :#{@tenantContext.getTenantId()} " +
           "ORDER BY c.totalSpent DESC")
    List<Customer> findTopCustomersBySpending(Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.organization.id = :#{@tenantContext.getTenantId()} " +
           "ORDER BY c.totalReservations DESC")
    List<Customer> findTopCustomersByReservations(Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.organization.id = :#{@tenantContext.getTenantId()} " +
           "ORDER BY c.customerLifetimeValue DESC")
    List<Customer> findTopCustomersByLifetimeValue(Pageable pageable);
}