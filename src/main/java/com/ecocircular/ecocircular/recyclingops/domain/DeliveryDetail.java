package com.ecocircular.ecocircular.recyclingops.domain;

import com.ecocircular.ecocircular.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "delivery_details")
@Getter @Setter @NoArgsConstructor
@AttributeOverride(name = "tenantId", column = @Column(name = "tenant_id", insertable = false, updatable = false))
@AttributeOverride(name = "id", column = @Column(name = "tenant_id", insertable = false, updatable = false))
public class DeliveryDetail extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "material_id", nullable = false)
    private MaterialCatalog material;

    @Column(nullable = false)
    private double quantity;

    @Column(nullable = false)
    private int pointsEarned;

    @Column(name = "co2_estimated", precision = 10, scale = 4)
    private BigDecimal co2Estimated;
}