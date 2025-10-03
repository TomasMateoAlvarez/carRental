package com.example.carrental.services;

import com.example.carrental.dto.DashboardKPIsDTO;
import com.example.carrental.enums.ReservationStatus;
import com.example.carrental.enums.VehicleStatus;
import com.example.carrental.repository.ReservationRepository;
import com.example.carrental.repository.UserRepository;
import com.example.carrental.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final ReservationRepository reservationRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    public DashboardKPIsDTO getDashboardKPIs() {
        log.info("Calculating dashboard KPIs");

        return DashboardKPIsDTO.builder()
                .totalRevenue(calculateTotalRevenue())
                .monthlyRevenue(calculateMonthlyRevenue())
                .dailyRevenue(calculateDailyRevenue())
                .revenueGrowth(calculateRevenueGrowth())
                .totalVehicles(getTotalVehicles())
                .availableVehicles(getAvailableVehicles())
                .rentedVehicles(getRentedVehicles())
                .maintenanceVehicles(getMaintenanceVehicles())
                .utilizationRate(calculateUtilizationRate())
                .totalReservations(getTotalReservations())
                .activeReservations(getActiveReservations())
                .pendingReservations(getPendingReservations())
                .completedReservations(getCompletedReservations())
                .cancelledReservations(getCancelledReservations())
                .cancellationRate(calculateCancellationRate())
                .totalCustomers(getTotalCustomers())
                .newCustomersThisMonth(getNewCustomersThisMonth())
                .repeatCustomers(getRepeatCustomers())
                .customerRetentionRate(calculateCustomerRetentionRate())
                .averageRentalDuration(calculateAverageRentalDuration())
                .averageRevenuePerRental(calculateAverageRevenuePerRental())
                .bookingConversionRate(calculateBookingConversionRate())
                .lastUpdated(LocalDateTime.now())
                .period("monthly")
                .build();
    }

    // Revenue Calculations
    private BigDecimal calculateTotalRevenue() {
        return reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.COMPLETED)
                .map(r -> r.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateMonthlyRevenue() {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        return reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.COMPLETED)
                .filter(r -> r.getCreatedAt().toLocalDate().isAfter(startOfMonth.minusDays(1)))
                .map(r -> r.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDailyRevenue() {
        LocalDate today = LocalDate.now();
        return reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.COMPLETED)
                .filter(r -> r.getCreatedAt().toLocalDate().equals(today))
                .map(r -> r.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Double calculateRevenueGrowth() {
        // Simple calculation: current month vs previous month
        LocalDate startOfCurrentMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate startOfPreviousMonth = startOfCurrentMonth.minusMonths(1);

        BigDecimal currentMonthRevenue = calculateMonthlyRevenue();
        BigDecimal previousMonthRevenue = reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.COMPLETED)
                .filter(r -> {
                    LocalDate createdDate = r.getCreatedAt().toLocalDate();
                    return createdDate.isAfter(startOfPreviousMonth.minusDays(1)) &&
                           createdDate.isBefore(startOfCurrentMonth);
                })
                .map(r -> r.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (previousMonthRevenue.compareTo(BigDecimal.ZERO) == 0) {
            return currentMonthRevenue.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }

        return currentMonthRevenue.subtract(previousMonthRevenue)
                .divide(previousMonthRevenue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    // Fleet Calculations
    private Integer getTotalVehicles() {
        return (int) vehicleRepository.count();
    }

    private Integer getAvailableVehicles() {
        return (int) vehicleRepository.countByStatus(VehicleStatus.AVAILABLE);
    }

    private Integer getRentedVehicles() {
        return (int) vehicleRepository.countByStatus(VehicleStatus.RENTED);
    }

    private Integer getMaintenanceVehicles() {
        return (int) (vehicleRepository.countByStatus(VehicleStatus.MAINTENANCE) +
                     vehicleRepository.countByStatus(VehicleStatus.IN_REPAIR));
    }

    private Double calculateUtilizationRate() {
        int total = getTotalVehicles();
        if (total == 0) return 0.0;
        return (double) getRentedVehicles() / total * 100.0;
    }

    // Reservation Calculations
    private Long getTotalReservations() {
        return reservationRepository.count();
    }

    private Long getActiveReservations() {
        return reservationRepository.countByStatus(ReservationStatus.IN_PROGRESS);
    }

    private Long getPendingReservations() {
        return reservationRepository.countByStatus(ReservationStatus.PENDING);
    }

    private Long getCompletedReservations() {
        return reservationRepository.countByStatus(ReservationStatus.COMPLETED);
    }

    private Long getCancelledReservations() {
        return reservationRepository.countByStatus(ReservationStatus.CANCELLED);
    }

    private Double calculateCancellationRate() {
        long total = getTotalReservations();
        if (total == 0) return 0.0;
        return (double) getCancelledReservations() / total * 100.0;
    }

    // Customer Calculations
    private Long getTotalCustomers() {
        return userRepository.count();
    }

    private Long getNewCustomersThisMonth() {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        return userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt().toLocalDate().isAfter(startOfMonth.minusDays(1)))
                .count();
    }

    private Long getRepeatCustomers() {
        // Customers with more than one completed reservation
        return userRepository.findAll().stream()
                .filter(u -> reservationRepository.findAll().stream()
                        .filter(r -> r.getUser().getId().equals(u.getId()))
                        .filter(r -> r.getStatus() == ReservationStatus.COMPLETED)
                        .count() > 1)
                .count();
    }

    private Double calculateCustomerRetentionRate() {
        long total = getTotalCustomers();
        if (total == 0) return 0.0;
        return (double) getRepeatCustomers() / total * 100.0;
    }

    // Performance Calculations
    private Double calculateAverageRentalDuration() {
        return reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.COMPLETED)
                .mapToInt(r -> r.getTotalDays())
                .average()
                .orElse(0.0);
    }

    private BigDecimal calculateAverageRevenuePerRental() {
        List<BigDecimal> revenues = reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.COMPLETED)
                .map(r -> r.getTotalAmount())
                .toList();

        if (revenues.isEmpty()) return BigDecimal.ZERO;

        BigDecimal total = revenues.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(revenues.size()), 2, RoundingMode.HALF_UP);
    }

    private Double calculateBookingConversionRate() {
        long total = getTotalReservations();
        if (total == 0) return 0.0;
        long confirmed = reservationRepository.countByStatus(ReservationStatus.CONFIRMED) +
                        reservationRepository.countByStatus(ReservationStatus.IN_PROGRESS) +
                        reservationRepository.countByStatus(ReservationStatus.COMPLETED);
        return (double) confirmed / total * 100.0;
    }

    // Chart Data Methods
    public Map<String, Object> getRevenueChart(int days) {
        Map<String, Object> chartData = new HashMap<>();
        // Implementation for revenue chart data
        chartData.put("labels", List.of("Week 1", "Week 2", "Week 3", "Week 4"));
        chartData.put("data", List.of(1200, 1800, 2200, 2800));
        chartData.put("period", days + " days");
        return chartData;
    }

    public Map<String, Object> getVehicleUtilization() {
        Map<String, Object> utilizationData = new HashMap<>();
        utilizationData.put("available", getAvailableVehicles());
        utilizationData.put("rented", getRentedVehicles());
        utilizationData.put("maintenance", getMaintenanceVehicles());
        utilizationData.put("total", getTotalVehicles());
        return utilizationData;
    }

    public Map<String, Object> getReservationTrends(int days) {
        Map<String, Object> trendsData = new HashMap<>();
        trendsData.put("pending", getPendingReservations());
        trendsData.put("confirmed", reservationRepository.countByStatus(ReservationStatus.CONFIRMED));
        trendsData.put("completed", getCompletedReservations());
        trendsData.put("cancelled", getCancelledReservations());
        trendsData.put("period", days + " days");
        return trendsData;
    }
}