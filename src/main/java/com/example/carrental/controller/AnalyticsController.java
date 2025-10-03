package com.example.carrental.controller;

import com.example.carrental.dto.AnalyticsInsightDTO;
import com.example.carrental.dto.CustomerSegmentDTO;
import com.example.carrental.dto.PredictiveAnalyticsDTO;
import com.example.carrental.services.AdvancedAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class AnalyticsController {

    private final AdvancedAnalyticsService analyticsService;

    @GetMapping("/customer-lifetime-value")
    public ResponseEntity<Map<String, Object>> getCustomerLifetimeValue() {
        Map<String, Object> clv = analyticsService.calculateCustomerLifetimeValue();
        return ResponseEntity.ok(clv);
    }

    @GetMapping("/demand-forecast")
    public ResponseEntity<PredictiveAnalyticsDTO> getDemandForecast(
            @RequestParam(defaultValue = "30") int daysAhead) {
        PredictiveAnalyticsDTO forecast = analyticsService.predictDemand(daysAhead);
        return ResponseEntity.ok(forecast);
    }

    @GetMapping("/vehicle-performance")
    public ResponseEntity<Map<String, Object>> getVehiclePerformance() {
        Map<String, Object> performance = analyticsService.analyzeVehiclePerformance();
        return ResponseEntity.ok(performance);
    }

    @GetMapping("/churn-prediction")
    public ResponseEntity<List<CustomerSegmentDTO>> getChurnPrediction() {
        List<CustomerSegmentDTO> churnRisk = analyticsService.predictCustomerChurn();
        return ResponseEntity.ok(churnRisk);
    }

    @GetMapping("/price-optimization")
    public ResponseEntity<Map<String, Object>> getPriceOptimization() {
        Map<String, Object> insights = analyticsService.getPriceOptimizationInsights();
        return ResponseEntity.ok(insights);
    }

    @GetMapping("/revenue-optimization")
    public ResponseEntity<AnalyticsInsightDTO> getRevenueOptimization() {
        AnalyticsInsightDTO insights = analyticsService.getRevenueOptimizationInsights();
        return ResponseEntity.ok(insights);
    }
}