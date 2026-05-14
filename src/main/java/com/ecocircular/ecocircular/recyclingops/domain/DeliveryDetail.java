package com.ecocircular.ecocircular.recyclingops.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Embeddable
@Getter @Setter @NoArgsConstructor
public class DeliveryDetail {

    private UUID materialId;        // Referencia a MaterialCatalog (por ID)

    private double quantity;
    private int pointsEarned;

    @Column(name = "co2_estimated")
    private BigDecimal co2Estimated;
}