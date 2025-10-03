package com.example.carrental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {

    private Long tenantId;
    private String reportType;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Map<String, Object> data;
    private LocalDateTime generatedAt;
}