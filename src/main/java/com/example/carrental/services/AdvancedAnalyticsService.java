package com.example.carrental.services;

import com.example.carrental.dto.AnalyticsInsightDTO;
import com.example.carrental.dto.PredictiveAnalyticsDTO;
import com.example.carrental.dto.CustomerSegmentDTO;
import com.example.carrental.model.Reservation;
import com.example.carrental.model.User;
import com.example.carrental.model.VehicleModel;
import com.example.carrental.repository.ReservationRepository;
import com.example.carrental.repository.UserRepository;
import com.example.carrental.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvancedAnalyticsService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    // Customer Lifetime Value Analysis
    public Map<String, Object> calculateCustomerLifetimeValue() {
        List<User> users = userRepository.findAll();
        Map<String, Object> clvAnalysis = new HashMap<>();

        List<CustomerSegmentDTO> segments = users.stream()
                .map(this::analyzeCustomer)
                .collect(Collectors.groupingBy(CustomerSegmentDTO::getSegment))
                .entrySet().stream()
                .map(entry -> {
                    String segment = entry.getKey();
                    List<CustomerSegmentDTO> customers = entry.getValue();

                    double avgCLV = customers.stream()
                            .mapToDouble(CustomerSegmentDTO::getLifetimeValue)
                            .average().orElse(0.0);

                    return CustomerSegmentDTO.builder()
                            .segment(segment)
                            .customerCount(customers.size())
                            .averageLifetimeValue(avgCLV)
                            .totalValue(customers.stream().mapToDouble(CustomerSegmentDTO::getLifetimeValue).sum())
                            .build();
                })
                .collect(Collectors.toList());

        clvAnalysis.put("segments", segments);
        clvAnalysis.put("totalCustomers", users.size());
        clvAnalysis.put("averageCLV", segments.stream().mapToDouble(CustomerSegmentDTO::getAverageLifetimeValue).average().orElse(0.0));

        return clvAnalysis;
    }

    // Demand Forecasting
    public PredictiveAnalyticsDTO predictDemand(int daysAhead) {
        LocalDate startDate = LocalDate.now().minusDays(365); // Use last year's data
        LocalDate endDate = LocalDate.now();

        List<Reservation> historicalReservations = reservationRepository
                .findReservationsByDateRange(startDate, endDate);

        // Group by day of week and calculate averages
        Map<Integer, Double> weeklyPatterns = historicalReservations.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getStartDate().getDayOfWeek().getValue(),
                        Collectors.averagingDouble(r -> 1.0) // Count reservations
                ));

        // Seasonal analysis (monthly patterns)
        Map<Integer, Double> seasonalPatterns = historicalReservations.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getStartDate().getMonthValue(),
                        Collectors.averagingDouble(r -> 1.0)
                ));

        // Predict demand for next period
        List<PredictiveAnalyticsDTO.DemandForecast> forecasts = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();

        for (int i = 0; i < daysAhead; i++) {
            LocalDate forecastDate = currentDate.plusDays(i);
            int dayOfWeek = forecastDate.getDayOfWeek().getValue();
            int month = forecastDate.getMonthValue();

            double weeklyMultiplier = weeklyPatterns.getOrDefault(dayOfWeek, 1.0);
            double seasonalMultiplier = seasonalPatterns.getOrDefault(month, 1.0);

            // Simple ML-like calculation (in production, use actual ML models)
            double predictedDemand = (weeklyMultiplier + seasonalMultiplier) / 2;

            forecasts.add(PredictiveAnalyticsDTO.DemandForecast.builder()
                    .date(forecastDate)
                    .predictedReservations((int) Math.round(predictedDemand * 10)) // Scale up
                    .confidence(calculateConfidence(weeklyMultiplier, seasonalMultiplier))
                    .build());
        }

        return PredictiveAnalyticsDTO.builder()
                .forecastPeriodDays(daysAhead)
                .demandForecasts(forecasts)
                .weeklyPatterns(weeklyPatterns)
                .seasonalPatterns(seasonalPatterns)
                .modelAccuracy(calculateModelAccuracy())
                .generatedAt(LocalDateTime.now())
                .build();
    }

    // Vehicle Performance Analytics
    public Map<String, Object> analyzeVehiclePerformance() {
        List<VehicleModel> vehicles = vehicleRepository.findAll();
        Map<String, Object> performance = new HashMap<>();

        List<Map<String, Object>> vehicleMetrics = vehicles.stream()
                .map(vehicle -> {
                    List<Reservation> vehicleReservations = reservationRepository
                            .findActiveReservationsByVehicle(vehicle);

                    Map<String, Object> metrics = new HashMap<>();
                    metrics.put("vehicleId", vehicle.getId());
                    metrics.put("licensePlate", vehicle.getLicensePlate());
                    metrics.put("brand", vehicle.getBrand());
                    metrics.put("model", vehicle.getModel());

                    // Utilization rate
                    long totalDays = ChronoUnit.DAYS.between(
                            LocalDate.now().minusDays(365), LocalDate.now());
                    long rentedDays = vehicleReservations.stream()
                            .mapToLong(r -> ChronoUnit.DAYS.between(r.getStartDate(), r.getEndDate()))
                            .sum();

                    double utilizationRate = (double) rentedDays / totalDays * 100;
                    metrics.put("utilizationRate", Math.round(utilizationRate * 100.0) / 100.0);

                    // Revenue generation
                    BigDecimal totalRevenue = vehicleReservations.stream()
                            .map(Reservation::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    metrics.put("totalRevenue", totalRevenue);

                    // Revenue per day
                    BigDecimal revenuePerDay = totalRevenue.divide(
                            BigDecimal.valueOf(Math.max(rentedDays, 1)), 2, RoundingMode.HALF_UP);
                    metrics.put("revenuePerDay", revenuePerDay);

                    return metrics;
                })
                .sorted((a, b) -> Double.compare(
                        (Double) b.get("utilizationRate"),
                        (Double) a.get("utilizationRate")))
                .collect(Collectors.toList());

        performance.put("vehicles", vehicleMetrics);
        performance.put("topPerformers", vehicleMetrics.stream().limit(5).collect(Collectors.toList()));
        performance.put("underPerformers", vehicleMetrics.stream()
                .filter(v -> (Double) v.get("utilizationRate") < 30.0)
                .collect(Collectors.toList()));

        return performance;
    }

    // Churn Prediction
    public List<CustomerSegmentDTO> predictCustomerChurn() {
        List<User> users = userRepository.findAll();
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        LocalDate sixtyDaysAgo = LocalDate.now().minusDays(60);

        return users.stream()
                .map(user -> {
                    List<Reservation> recentReservations = reservationRepository.findByUserOrderByCreatedAtDesc(user)
                            .stream()
                            .filter(r -> r.getCreatedAt().toLocalDate().isAfter(thirtyDaysAgo))
                            .collect(Collectors.toList());

                    List<Reservation> previousReservations = reservationRepository.findByUserOrderByCreatedAtDesc(user)
                            .stream()
                            .filter(r -> r.getCreatedAt().toLocalDate().isBefore(thirtyDaysAgo) &&
                                        r.getCreatedAt().toLocalDate().isAfter(sixtyDaysAgo))
                            .collect(Collectors.toList());

                    // Calculate churn risk
                    double churnRisk = calculateChurnRisk(user, recentReservations, previousReservations);
                    String riskLevel = churnRisk > 0.7 ? "HIGH" : churnRisk > 0.4 ? "MEDIUM" : "LOW";

                    return CustomerSegmentDTO.builder()
                            .userId(user.getId())
                            .userEmail(user.getEmail())
                            .userFullName(user.getFirstName() + " " + user.getLastName())
                            .churnRisk(churnRisk)
                            .riskLevel(riskLevel)
                            .lastReservationDate(getLastReservationDate(user))
                            .totalReservations(reservationRepository.findByUserOrderByCreatedAtDesc(user).size())
                            .build();
                })
                .filter(customer -> "HIGH".equals(customer.getRiskLevel()) || "MEDIUM".equals(customer.getRiskLevel()))
                .sorted((a, b) -> Double.compare(b.getChurnRisk(), a.getChurnRisk()))
                .collect(Collectors.toList());
    }

    // Price Optimization Insights
    public Map<String, Object> getPriceOptimizationInsights() {
        List<Reservation> reservations = reservationRepository.findAll();
        Map<String, Object> insights = new HashMap<>();

        // Price sensitivity analysis
        Map<String, List<Reservation>> priceRanges = reservations.stream()
                .collect(Collectors.groupingBy(r -> {
                    double dailyRate = r.getDailyRate().doubleValue();
                    if (dailyRate < 30) return "Budget (< $30)";
                    if (dailyRate < 60) return "Standard ($30-60)";
                    if (dailyRate < 100) return "Premium ($60-100)";
                    return "Luxury ($100+)";
                }));

        Map<String, Object> priceAnalysis = priceRanges.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            List<Reservation> rangeReservations = entry.getValue();
                            Map<String, Object> stats = new HashMap<>();
                            stats.put("count", rangeReservations.size());
                            stats.put("averageDuration", rangeReservations.stream()
                                    .mapToInt(Reservation::getTotalDays)
                                    .average().orElse(0.0));
                            stats.put("totalRevenue", rangeReservations.stream()
                                    .map(Reservation::getTotalAmount)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add));
                            return stats;
                        }
                ));

        insights.put("priceRangeAnalysis", priceAnalysis);

        // Optimal pricing recommendations
        insights.put("recommendations", generatePricingRecommendations(priceAnalysis));

        return insights;
    }

    // Revenue Optimization
    public AnalyticsInsightDTO getRevenueOptimizationInsights() {
        List<Reservation> reservations = reservationRepository.findAll();

        // Peak hours analysis
        Map<Integer, Long> hourlyBookings = reservations.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getCreatedAt().getHour(),
                        Collectors.counting()
                ));

        // Peak days analysis
        Map<Integer, Long> dailyBookings = reservations.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getStartDate().getDayOfWeek().getValue(),
                        Collectors.counting()
                ));

        Map<String, Object> insights = new HashMap<>();
        insights.put("peakBookingHours", hourlyBookings);
        insights.put("peakBookingDays", dailyBookings);
        insights.put("optimalPricingWindows", identifyOptimalPricingWindows(hourlyBookings, dailyBookings));

        return AnalyticsInsightDTO.builder()
                .category("Revenue Optimization")
                .insights(insights)
                .recommendations(generateRevenueRecommendations(insights))
                .confidence(0.85)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    // Helper methods
    private CustomerSegmentDTO analyzeCustomer(User user) {
        List<Reservation> userReservations = reservationRepository.findByUserOrderByCreatedAtDesc(user);

        double lifetimeValue = userReservations.stream()
                .map(Reservation::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doubleValue();

        String segment = categorizeCustomer(lifetimeValue, userReservations.size());

        return CustomerSegmentDTO.builder()
                .userId(user.getId())
                .userEmail(user.getEmail())
                .userFullName(user.getFirstName() + " " + user.getLastName())
                .segment(segment)
                .lifetimeValue(lifetimeValue)
                .totalReservations(userReservations.size())
                .build();
    }

    private String categorizeCustomer(double lifetimeValue, int reservationCount) {
        if (lifetimeValue > 1000 && reservationCount > 5) return "VIP";
        if (lifetimeValue > 500 && reservationCount > 3) return "Premium";
        if (lifetimeValue > 200 && reservationCount > 1) return "Regular";
        return "New";
    }

    private double calculateChurnRisk(User user, List<Reservation> recent, List<Reservation> previous) {
        if (previous.isEmpty()) return 0.3; // New customers have medium risk

        double recentActivity = recent.size();
        double previousActivity = previous.size();

        if (recentActivity == 0 && previousActivity > 0) return 0.9; // High risk
        if (recentActivity < previousActivity * 0.5) return 0.6; // Medium-high risk

        return 0.2; // Low risk
    }

    private LocalDate getLastReservationDate(User user) {
        return reservationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .findFirst()
                .map(r -> r.getCreatedAt().toLocalDate())
                .orElse(null);
    }

    private double calculateConfidence(double weeklyMultiplier, double seasonalMultiplier) {
        // Simple confidence calculation based on data consistency
        return Math.min(0.95, Math.max(0.60, (weeklyMultiplier + seasonalMultiplier) / 2 * 0.8));
    }

    private double calculateModelAccuracy() {
        // In production, this would be calculated using actual vs predicted comparisons
        return 0.78; // Mock accuracy
    }

    private List<String> generatePricingRecommendations(Map<String, Object> priceAnalysis) {
        List<String> recommendations = new ArrayList<>();
        recommendations.add("Consider dynamic pricing during peak hours");
        recommendations.add("Implement seasonal pricing adjustments");
        recommendations.add("Offer loyalty discounts for repeat customers");
        return recommendations;
    }

    private List<String> identifyOptimalPricingWindows(Map<Integer, Long> hourly, Map<Integer, Long> daily) {
        List<String> windows = new ArrayList<>();
        windows.add("Peak demand: Weekends and evenings (15-18h)");
        windows.add("Low demand: Weekday mornings (6-9h) - opportunity for promotions");
        return windows;
    }

    private List<String> generateRevenueRecommendations(Map<String, Object> insights) {
        return List.of(
                "Implement surge pricing during peak hours",
                "Offer early bird discounts for advance bookings",
                "Create weekend packages for family customers",
                "Develop corporate rates for business travelers"
        );
    }
}