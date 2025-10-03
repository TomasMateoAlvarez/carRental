package com.example.carrental.services;

import com.example.carrental.dto.ReportDTO;
import com.example.carrental.dto.FinancialReportDTO;
import com.example.carrental.model.Tenant;
import com.example.carrental.model.Reservation;
import com.example.carrental.model.Payment;
import com.example.carrental.repository.TenantRepository;
import com.example.carrental.repository.ReservationRepository;
import com.example.carrental.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportingService {

    private final TenantRepository tenantRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;
    private final PdfGeneratorService pdfGeneratorService;

    // Automated Daily Reports
    @Scheduled(cron = "0 0 8 * * *") // Every day at 8 AM
    public void generateDailyReports() {
        log.info("Starting daily report generation");

        List<Tenant> activeTenants = tenantRepository.findByIsActiveTrue();

        for (Tenant tenant : activeTenants) {
            try {
                FinancialReportDTO dailyReport = generateDailyFinancialReport(tenant.getId());

                // Send report via email
                Map<String, Object> reportData = convertReportToMap(dailyReport);
                notificationService.sendDailyReportEmail(reportData);

                log.info("Daily report generated for tenant: {}", tenant.getCompanyName());
            } catch (Exception e) {
                log.error("Failed to generate daily report for tenant: {}", tenant.getCompanyName(), e);
            }
        }
    }

    // Weekly Business Intelligence Reports
    @Scheduled(cron = "0 0 9 * * MON") // Every Monday at 9 AM
    public void generateWeeklyBusinessReports() {
        log.info("Starting weekly business report generation");

        List<Tenant> enterpriseTenants = tenantRepository.findBySubscriptionPlanAndIsActiveTrue(
                Tenant.SubscriptionPlan.ENTERPRISE, true);

        for (Tenant tenant : enterpriseTenants) {
            if (tenant.canAccessFeature("advanced_analytics")) {
                ReportDTO weeklyReport = generateWeeklyBusinessReport(tenant.getId());
                // Send advanced analytics report
                log.info("Weekly business report generated for: {}", tenant.getCompanyName());
            }
        }
    }

    // Monthly Subscription Billing
    @Scheduled(cron = "0 0 10 1 * *") // First day of month at 10 AM
    public void generateMonthlyInvoices() {
        log.info("Starting monthly invoice generation");

        List<Tenant> activeTenants = tenantRepository.findByIsActiveTrue();

        for (Tenant tenant : activeTenants) {
            try {
                if (tenant.isSubscriptionActive()) {
                    FinancialReportDTO invoiceData = generateMonthlyInvoice(tenant.getId());

                    // Generate PDF invoice
                    byte[] invoicePdf = pdfGeneratorService.generateInvoicePdf(invoiceData);

                    // Process subscription payment (if auto-billing enabled)
                    // paymentService.processSubscriptionPayment(tenant);

                    log.info("Monthly invoice generated for: {}", tenant.getCompanyName());
                }
            } catch (Exception e) {
                log.error("Failed to generate invoice for tenant: {}", tenant.getCompanyName(), e);
            }
        }
    }

    // Custom Report Generation
    public ReportDTO generateCustomReport(Long tenantId, LocalDate startDate, LocalDate endDate, String reportType) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        return switch (reportType.toUpperCase()) {
            case "FINANCIAL" -> generateFinancialReport(tenantId, startDate, endDate);
            case "OPERATIONS" -> generateOperationsReport(tenantId, startDate, endDate);
            case "CUSTOMER" -> generateCustomerReport(tenantId, startDate, endDate);
            case "VEHICLE_PERFORMANCE" -> generateVehiclePerformanceReport(tenantId, startDate, endDate);
            case "REVENUE_OPTIMIZATION" -> generateRevenueOptimizationReport(tenantId, startDate, endDate);
            default -> throw new RuntimeException("Unknown report type: " + reportType);
        };
    }

    private FinancialReportDTO generateDailyFinancialReport(Long tenantId) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        return generateFinancialReportForDate(tenantId, yesterday);
    }

    private FinancialReportDTO generateMonthlyInvoice(Long tenantId) {
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        LocalDate startOfMonth = lastMonth.withDayOfMonth(1);
        LocalDate endOfMonth = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth());

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        List<Reservation> monthlyReservations = reservationRepository
                .findByTenantAndDateRange(tenant, startOfMonth, endOfMonth);

        List<Payment> monthlyPayments = paymentRepository
                .findByTenantAndDateRange(tenant, startOfMonth, endOfMonth);

        // Calculate subscription fees
        BigDecimal subscriptionFee = calculateSubscriptionFee(tenant);
        BigDecimal usageFees = calculateUsageFees(tenant, monthlyReservations);
        BigDecimal totalRevenue = monthlyPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return FinancialReportDTO.builder()
                .tenantId(tenantId)
                .reportType("MONTHLY_INVOICE")
                .periodStart(startOfMonth)
                .periodEnd(endOfMonth)
                .subscriptionFee(subscriptionFee)
                .usageFees(usageFees)
                .totalRevenue(totalRevenue)
                .totalReservations(monthlyReservations.size())
                .invoiceNumber(generateInvoiceNumber(tenant))
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private ReportDTO generateWeeklyBusinessReport(Long tenantId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        List<Reservation> weeklyReservations = reservationRepository
                .findByTenantAndDateRange(tenant, startDate, endDate);

        Map<String, Object> analytics = new HashMap<>();

        // Key Performance Indicators
        analytics.put("totalReservations", weeklyReservations.size());
        analytics.put("weekOverWeekGrowth", calculateWeekOverWeekGrowth(tenant));
        analytics.put("averageReservationValue", calculateAverageReservationValue(weeklyReservations));
        analytics.put("topPerformingVehicles", getTopPerformingVehicles(weeklyReservations));
        analytics.put("customerSatisfactionScore", calculateCustomerSatisfaction(tenant));
        analytics.put("operationalEfficiency", calculateOperationalEfficiency(tenant));

        // Predictive Insights
        analytics.put("demandForecast", generateDemandForecast(tenant, 14));
        analytics.put("revenueProjection", generateRevenueProjection(tenant, 30));
        analytics.put("recommendations", generateBusinessRecommendations(analytics));

        return ReportDTO.builder()
                .tenantId(tenantId)
                .reportType("WEEKLY_BUSINESS")
                .periodStart(startDate)
                .periodEnd(endDate)
                .data(analytics)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private ReportDTO generateFinancialReport(Long tenantId, LocalDate startDate, LocalDate endDate) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        List<Payment> payments = paymentRepository.findByTenantAndDateRange(tenant, startDate, endDate);
        List<Reservation> reservations = reservationRepository.findByTenantAndDateRange(tenant, startDate, endDate);

        Map<String, Object> financialData = new HashMap<>();

        // Revenue Analysis
        BigDecimal totalRevenue = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageDailyRevenue = totalRevenue.divide(
                BigDecimal.valueOf(startDate.until(endDate).getDays()), 2, BigDecimal.ROUND_HALF_UP);

        // Revenue Breakdown
        Map<String, BigDecimal> revenueByCategory = payments.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getReservation().getVehicle().getCategory(),
                        Collectors.mapping(Payment::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        financialData.put("totalRevenue", totalRevenue);
        financialData.put("averageDailyRevenue", averageDailyRevenue);
        financialData.put("revenueByCategory", revenueByCategory);
        financialData.put("totalTransactions", payments.size());
        financialData.put("averageTransactionValue", totalRevenue.divide(BigDecimal.valueOf(payments.size()), 2, BigDecimal.ROUND_HALF_UP));

        return ReportDTO.builder()
                .tenantId(tenantId)
                .reportType("FINANCIAL")
                .periodStart(startDate)
                .periodEnd(endDate)
                .data(financialData)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private ReportDTO generateOperationsReport(Long tenantId, LocalDate startDate, LocalDate endDate) {
        // Implementation for operations metrics
        Map<String, Object> operationsData = new HashMap<>();
        // Fleet utilization, maintenance schedules, operational efficiency, etc.

        return ReportDTO.builder()
                .tenantId(tenantId)
                .reportType("OPERATIONS")
                .periodStart(startDate)
                .periodEnd(endDate)
                .data(operationsData)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private ReportDTO generateCustomerReport(Long tenantId, LocalDate startDate, LocalDate endDate) {
        // Implementation for customer analytics
        Map<String, Object> customerData = new HashMap<>();
        // Customer acquisition, retention, lifetime value, satisfaction, etc.

        return ReportDTO.builder()
                .tenantId(tenantId)
                .reportType("CUSTOMER")
                .periodStart(startDate)
                .periodEnd(endDate)
                .data(customerData)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private ReportDTO generateVehiclePerformanceReport(Long tenantId, LocalDate startDate, LocalDate endDate) {
        // Implementation for vehicle performance metrics
        Map<String, Object> vehicleData = new HashMap<>();
        // Utilization rates, revenue per vehicle, maintenance costs, depreciation, etc.

        return ReportDTO.builder()
                .tenantId(tenantId)
                .reportType("VEHICLE_PERFORMANCE")
                .periodStart(startDate)
                .periodEnd(endDate)
                .data(vehicleData)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private ReportDTO generateRevenueOptimizationReport(Long tenantId, LocalDate startDate, LocalDate endDate) {
        // Implementation for revenue optimization insights
        Map<String, Object> revenueData = new HashMap<>();
        // Pricing optimization, demand patterns, revenue opportunities, etc.

        return ReportDTO.builder()
                .tenantId(tenantId)
                .reportType("REVENUE_OPTIMIZATION")
                .periodStart(startDate)
                .periodEnd(endDate)
                .data(revenueData)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    // Helper methods
    private FinancialReportDTO generateFinancialReportForDate(Long tenantId, LocalDate date) {
        // Implementation for single-date financial report
        return FinancialReportDTO.builder()
                .tenantId(tenantId)
                .reportType("DAILY_FINANCIAL")
                .periodStart(date)
                .periodEnd(date)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private BigDecimal calculateSubscriptionFee(Tenant tenant) {
        return switch (tenant.getSubscriptionPlan()) {
            case STARTER -> BigDecimal.valueOf(49.00);
            case BUSINESS -> BigDecimal.valueOf(199.00);
            case ENTERPRISE -> BigDecimal.valueOf(499.00);
        };
    }

    private BigDecimal calculateUsageFees(Tenant tenant, List<Reservation> reservations) {
        // Calculate usage-based fees (e.g., per reservation, per vehicle, etc.)
        BigDecimal baseFee = BigDecimal.valueOf(2.50); // Per reservation
        return baseFee.multiply(BigDecimal.valueOf(reservations.size()));
    }

    private String generateInvoiceNumber(Tenant tenant) {
        String monthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        return String.format("INV-%s-%s", tenant.getTenantCode(), monthYear);
    }

    private Map<String, Object> convertReportToMap(FinancialReportDTO report) {
        Map<String, Object> data = new HashMap<>();
        data.put("report", report);
        data.put("generatedAt", LocalDateTime.now());
        return data;
    }

    // Additional helper methods for analytics
    private double calculateWeekOverWeekGrowth(Tenant tenant) {
        // Implementation for growth calculation
        return 0.0;
    }

    private BigDecimal calculateAverageReservationValue(List<Reservation> reservations) {
        if (reservations.isEmpty()) return BigDecimal.ZERO;
        BigDecimal total = reservations.stream()
                .map(Reservation::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(reservations.size()), 2, BigDecimal.ROUND_HALF_UP);
    }

    private List<Map<String, Object>> getTopPerformingVehicles(List<Reservation> reservations) {
        // Implementation for top performing vehicles analysis
        return new ArrayList<>();
    }

    private double calculateCustomerSatisfaction(Tenant tenant) {
        // Implementation for customer satisfaction score
        return 4.5; // Mock score
    }

    private double calculateOperationalEfficiency(Tenant tenant) {
        // Implementation for operational efficiency calculation
        return 0.85; // Mock efficiency score
    }

    private Map<String, Object> generateDemandForecast(Tenant tenant, int days) {
        // Implementation for demand forecasting
        return new HashMap<>();
    }

    private Map<String, Object> generateRevenueProjection(Tenant tenant, int days) {
        // Implementation for revenue projection
        return new HashMap<>();
    }

    private List<String> generateBusinessRecommendations(Map<String, Object> analytics) {
        // Implementation for AI-powered business recommendations
        return List.of(
                "Consider implementing dynamic pricing during peak hours",
                "Expand fleet in the compact car category based on demand",
                "Implement customer loyalty program to improve retention"
        );
    }
}