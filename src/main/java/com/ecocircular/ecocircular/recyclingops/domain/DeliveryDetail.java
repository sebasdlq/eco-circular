package com.ecocircular.ecocircular.recyclingops.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Embeddable
@Getter @Setter @NoArgsConstructor
public class DeliveryDetail {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private MaterialCatalog material;

    @Column(nullable = false)
    private double quantity;
    @Column(nullable = false)
    private int pointsEarned;

    @Column(name = "co2_estimated", precision = 10, scale = 4)
    private BigDecimal co2Estimated;
}