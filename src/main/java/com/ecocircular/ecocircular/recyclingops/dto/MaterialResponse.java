package com.ecocircular.ecocircular.recyclingops.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class MaterialResponse {
    private UUID id;
    private UUID tenantId;
    private String name;
    private String category;
    private String unit;
    private int pointsPerUnit;
    private double co2Factor;
    private String equivalents;
    private boolean active;
    private LocalDate effectiveFrom;
}
