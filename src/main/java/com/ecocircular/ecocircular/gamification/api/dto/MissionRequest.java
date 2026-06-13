package com.ecocircular.ecocircular.gamification.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor
public class MissionRequest {

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String targetType;

    @Positive
    private double targetValue;

    @Min(0)
    private int rewardPoints;

    private LocalDate deadline; // null = sin vencimiento fijo

    private UUID tenantId; // null = global
}