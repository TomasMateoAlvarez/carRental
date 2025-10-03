package com.example.carrental.controller;

import com.example.carrental.dto.DashboardKPIsDTO;
import com.example.carrental.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/kpis")
    public ResponseEntity<DashboardKPIsDTO> getDashboardKPIs() {
        DashboardKPIsDTO kpis = dashboardService.getDashboardKPIs();
        return ResponseEntity.ok(kpis);
    }

    @GetMapping("/revenue-chart")
    public ResponseEntity<?> getRevenueChart(@RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(dashboardService.getRevenueChart(days));
    }

    @GetMapping("/vehicle-utilization")
    public ResponseEntity<?> getVehicleUtilization() {
        return ResponseEntity.ok(dashboardService.getVehicleUtilization());
    }

    @GetMapping("/reservation-trends")
    public ResponseEntity<?> getReservationTrends(@RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(dashboardService.getReservationTrends(days));
    }
}