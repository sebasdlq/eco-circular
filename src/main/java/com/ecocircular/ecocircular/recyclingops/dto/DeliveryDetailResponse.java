package com.ecocircular.ecocircular.recyclingops.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter @Setter
public class DeliveryDetailResponse {

    private UUID materialId;
    private String materialName;
    private double quantity;
    private int pointsEarned;
    private BigDecimal co2Estimated;
}