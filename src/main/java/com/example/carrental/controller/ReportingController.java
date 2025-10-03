package com.example.carrental.controller;

import com.example.carrental.dto.ReportDTO;
import com.example.carrental.services.ReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class ReportingController {

    private final ReportingService reportingService;

    @GetMapping("/custom")
    public ResponseEntity<ReportDTO> generateCustomReport(
            @RequestParam Long tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String reportType) {

        ReportDTO report = reportingService.generateCustomReport(tenantId, startDate, endDate, reportType);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/daily/manual")
    public ResponseEntity<String> generateDailyReportsManually() {
        reportingService.generateDailyReports();
        return ResponseEntity.ok("Daily reports generation started");
    }

    @PostMapping("/weekly/manual")
    public ResponseEntity<String> generateWeeklyReportsManually() {
        reportingService.generateWeeklyBusinessReports();
        return ResponseEntity.ok("Weekly reports generation started");
    }

    @PostMapping("/monthly/manual")
    public ResponseEntity<String> generateMonthlyInvoicesManually() {
        reportingService.generateMonthlyInvoices();
        return ResponseEntity.ok("Monthly invoices generation started");
    }
}