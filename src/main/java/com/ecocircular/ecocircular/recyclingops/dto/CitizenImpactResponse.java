package com.ecocircular.ecocircular.recyclingops.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CitizenImpactResponse {
    private double totalKg;
    private double totalCo2;
    private int    totalPoints;
    private int    totalDeliveries;
}