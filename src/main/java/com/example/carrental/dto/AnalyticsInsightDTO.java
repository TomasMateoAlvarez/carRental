package com.example.carrental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsInsightDTO {

    private String category;
    private Map<String, Object> insights;
    private List<String> recommendations;
    private Double confidence;
    private LocalDateTime generatedAt;
}