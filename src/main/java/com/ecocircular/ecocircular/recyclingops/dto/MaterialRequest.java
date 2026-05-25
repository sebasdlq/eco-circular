package com.ecocircular.ecocircular.recyclingops.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class MaterialRequest {

    /** Null = material global (solo admins); con valor = material del tenant */
    private UUID tenantId;

    @NotBlank(message = "El nombre del material es obligatorio")
    private String name;

    @NotBlank(message = "La categoría es obligatoria")
    private String category;

    @NotBlank(message = "La unidad es obligatoria")
    private String unit;

    @Min(value = 0, message = "Los puntos por unidad deben ser >= 0")
    private int pointsPerUnit;

    @Min(value = 0, message = "El factor CO₂ debe ser >= 0")
    private double co2Factor;

    /** JSON libre con equivalencias (árboles, energía, etc.) */
    private String equivalents;

    private LocalDate effectiveFrom;
}