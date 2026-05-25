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

    @Positive(message = "Los puntos deben ser positivos")
    private int pointsEarned;

    private BigDecimal co2Estimated;
}