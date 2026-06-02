package com.ecocircular.ecocircular.recyclingops.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter @Setter
public class DeliveryDetailRequest {

    @NotNull(message = "El materialId es obligatorio")
    private UUID materialId;

    @Positive(message = "La cantidad debe ser positiva")
    private double quantity;

    // ✅ Eliminadas las validaciones de pointsEarned y co2Estimated
    // El backend los calcula automáticamente desde material_catalog
    private Integer pointsEarned;
    private BigDecimal co2Estimated;
}