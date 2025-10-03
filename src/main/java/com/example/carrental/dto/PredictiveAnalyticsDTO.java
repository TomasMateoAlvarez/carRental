package com.example.carrental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictiveAnalyticsDTO {

    private Integer forecastPeriodDays;
    private List<DemandForecast> demandForecasts;
    private Map<Integer, Double> weeklyPatterns;
    private Map<Integer, Double> seasonalPatterns;
    private Double modelAccuracy;
    private LocalDateTime generatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DemandForecast {
        private LocalDate date;
        private Integer predictedReservations;
        private Double confidence;
    }
}