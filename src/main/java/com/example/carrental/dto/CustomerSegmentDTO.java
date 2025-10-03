package com.example.carrental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSegmentDTO {

    private Long userId;
    private String userEmail;
    private String userFullName;
    private String segment;
    private Double lifetimeValue;
    private Double averageLifetimeValue;
    private Double totalValue;
    private Integer customerCount;
    private Integer totalReservations;
    private Double churnRisk;
    private String riskLevel;
    private LocalDate lastReservationDate;
}