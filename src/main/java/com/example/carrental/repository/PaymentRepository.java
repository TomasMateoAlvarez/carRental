package com.example.carrental.repository;

import com.example.carrental.model.Payment;
import com.example.carrental.model.Tenant;
import com.example.carrental.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByUserOrderByCreatedAtDesc(User user);

    Optional<Payment> findByPaymentCode(String paymentCode);

    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);

    @Query("SELECT p FROM Payment p WHERE p.tenant = :tenant AND DATE(p.createdAt) BETWEEN :startDate AND :endDate")
    List<Payment> findByTenantAndDateRange(@Param("tenant") Tenant tenant,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    @Query("SELECT p FROM Payment p WHERE p.user = :user AND p.status = 'COMPLETED'")
    List<Payment> findCompletedPaymentsByUser(@Param("user") User user);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND DATE(p.createdAt) = :date")
    Double getTotalRevenueForDate(@Param("date") LocalDate date);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND MONTH(p.createdAt) = :month AND YEAR(p.createdAt) = :year")
    Double getTotalRevenueForMonth(@Param("month") int month, @Param("year") int year);
}